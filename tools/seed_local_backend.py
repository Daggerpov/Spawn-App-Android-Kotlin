#!/usr/bin/env python3
"""Seed a LOCAL Spawn backend with test users, friendships, activities and chats.

Only for local development: point it at a backend running on your machine
(default http://localhost:8080/api/v1/) — never at production.

Usage:  python3 tools/seed_local_backend.py [base_url]

Every seeded account uses the test password below. Log in on the Android
emulator with username "daniel" (or any other seeded username) + that password.
"""
import json
import sys
import urllib.error
import urllib.request
from datetime import datetime, timedelta, timezone

BASE = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080/api/v1/"
if not any(h in BASE for h in ("localhost", "127.0.0.1", "10.0.2.2")):
    sys.exit("Refusing to seed a non-local backend: " + BASE)

TEST_PASSWORD = "spawntest123"

USERS = [
    ("daniel", "Daniel Agapov", "Building Spawn. Always down for dinner."),
    ("haley", "Haley Wong", "Climbing, coffee, chaos."),
    ("ethan", "Ethan Hansen", "Basketball 5 on 5s every Thursday."),
    ("rafi", "Rafi Arnouk", "Night owl. Ask me about billiards."),
    ("shannon", "Shannon Lee", "Study buddy for hire."),
    ("jenna", "Jenna Park", "Sushi > everything."),
]


def call(method, path, body=None, token=None, params=None):
    url = BASE + path
    if params:
        url += "?" + "&".join(f"{k}={v}" for k, v in params.items())
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(url, data=data, method=method)
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", "Bearer " + token)
    # Local-only: spread calls across fake client IPs so the per-IP auth rate limit doesn't stop seeding.
    import random
    req.add_header("X-Forwarded-For", f"10.{random.randint(0, 255)}.{random.randint(0, 255)}.{random.randint(1, 254)}")
    try:
        with urllib.request.urlopen(req) as resp:
            raw = resp.read().decode() or "null"
            auth = resp.headers.get("Authorization", "")
            return resp.status, json.loads(raw) if raw.strip().startswith(("{", "[")) else raw, auth.replace("Bearer ", "")
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode(), ""


def login(username):
    status, body, token = call("POST", "auth/login", {"usernameOrEmail": username, "password": TEST_PASSWORD})
    if status != 200:
        return None, None
    return body["user"], token


# Registration is email-code based and codes are bcrypt-hashed. Locally we
# overwrite the stored hash with a known test code so we can run the real
# onboarding endpoints. Needs the local MySQL from README (port 3307).
TEST_CODE = "424242"
TEST_CODE_BCRYPT = "$2y$10$ZkUhqcP4PvGaMLMAbcqnbu4gl5HQbOTLdw49gJzD00Eg8onp.J8C2"
MYSQL = ["/usr/local/mysql/bin/mysql", "-uroot", "-h127.0.0.1", "-P3307", "spawn", "-e"]


def ensure_user(username, name, bio):
    import subprocess
    user, token = login(username)
    if user:
        return user, token
    email = f"{username}@spawn.test"
    call("POST", "auth/register/verification/send", {"email": email})
    subprocess.run(MYSQL + [f"UPDATE email_verification SET verification_code='{TEST_CODE_BCRYPT}' WHERE email='{email}'"], check=True)
    status, body, token = call("POST", "auth/register/verification/check", {"email": email, "verificationCode": TEST_CODE})
    if status != 200:
        sys.exit(f"verify {username} failed: {status} {body}")
    uid = body["user"]["id"]
    status, body, token2 = call("POST", "auth/user/details", {"id": uid, "username": username, "phoneNumber": "+1604555" + str(1000 + [u for u, _, _ in USERS].index(username)), "password": TEST_PASSWORD}, token=token)
    if status != 200:
        sys.exit(f"details {username} failed: {status} {body}")
    token = token2 or token
    call("POST", f"users/{uid}/optional-details", {"name": name}, token=token)
    call("POST", f"auth/complete-contact-import/{uid}", token=token)
    call("POST", f"auth/accept-tos/{uid}", token=token)
    user, token = login(username)
    call("PATCH", f"users/{uid}", {"bio": bio, "name": name, "username": username}, token=token)
    return login(username)


def main():
    accounts = {u: ensure_user(u, n, b) for u, n, b in USERS}
    me, me_tok = accounts["daniel"]
    print("users:", {u: a[0]["id"] for u, a in accounts.items()})

    # Placeholder avatars (local DB only) so screenshots look like real accounts.
    import subprocess
    avatars = {"daniel": "men/32", "haley": "women/44", "ethan": "men/46", "rafi": "men/75", "shannon": "women/68", "jenna": "women/12"}
    for u, a in avatars.items():
        subprocess.run(MYSQL + [f"UPDATE user SET profile_picture_url_string='https://randomuser.me/api/portraits/{a}.jpg' WHERE username='{u}'"], check=True)
    subprocess.run(["redis-cli", "-p", "6380", "FLUSHALL"], capture_output=True)

    # Friendships: daniel <-> everyone except jenna (jenna sends daniel a request instead)
    for other in ("haley", "ethan", "rafi", "shannon"):
        o, o_tok = accounts[other]
        st, fr, _ = call("POST", "friend-requests", {"senderUserId": o["id"], "receiverUserId": me["id"]}, token=o_tok)
        if st in (200, 201) and isinstance(fr, dict) and fr.get("id"):
            call("PUT", f"friend-requests/{fr['id']}", token=me_tok, params={"friendRequestAction": "accept"})
    j, j_tok = accounts["jenna"]
    call("POST", "friend-requests", {"senderUserId": j["id"], "receiverUserId": me["id"]}, token=j_tok)
    # haley <-> ethan so "people met"/mutuals have data
    h, h_tok = accounts["haley"]
    e, e_tok = accounts["ethan"]
    st, fr, _ = call("POST", "friend-requests", {"senderUserId": h["id"], "receiverUserId": e["id"]}, token=h_tok)
    if st in (200, 201) and isinstance(fr, dict) and fr.get("id"):
        call("PUT", f"friend-requests/{fr['id']}", token=e_tok, params={"friendRequestAction": "accept"})

    now = datetime.now(timezone.utc).replace(microsecond=0)

    def iso(dt):
        return dt.isoformat().replace("+00:00", "Z")

    spots = {
        "chipotle": ("Chipotle, University Blvd", 49.2665, -123.2489),
        "pit": ("The Pit Pub", 49.2668, -123.2500),
        "srec": ("UBC Recreation Centre", 49.2681, -123.2486),
        "irving": ("Irving K. Barber Library", 49.2675, -123.2526),
        "koerner": ("Koerner's Pub", 49.2697, -123.2564),
    }

    def create(creator, title, icon, spot, start_in_min, dur_min, note, invite):
        c, c_tok = accounts[creator]
        name, lat, lng = spots[spot]
        types = call("GET", f"users/{c['id']}/activity-types", token=c_tok)[1]
        type_id = types[0]["id"] if isinstance(types, list) and types else None
        body = {
            "title": title, "icon": icon, "note": note,
            "startTime": iso(now + timedelta(minutes=start_in_min)),
            "endTime": iso(now + timedelta(minutes=start_in_min + dur_min)),
            "location": {"name": name, "latitude": lat, "longitude": lng},
            "activityTypeId": type_id, "creatorUserId": c["id"],
            "invitedUserIds": [accounts[i][0]["id"] for i in invite],
            "participantUserIds": [], "chatMessageIds": [],
            "clientTimezone": "America/Vancouver",
        }
        st, resp, _ = call("POST", "activities", body, token=c_tok)
        if st not in (200, 201):
            print("create activity failed", title, st, resp)
            return None
        return resp["activity"]["id"]

    a1 = create("daniel", "Dinner @ Chipotle", "🌯", "chipotle", 90, 90, "Come grab dinner with us!", ["haley", "ethan", "rafi", "shannon"])
    a2 = create("ethan", "Billiards Tourney", "🎱", "pit", 30, 150, "Bracket starts at 8, show up early", ["daniel", "haley", "rafi"])
    a3 = create("rafi", "Late Night Basketball", "🏀", "srec", 1, 120, "5 on 5s, bring water", ["daniel", "ethan"])
    a4 = create("shannon", "Study Sesh", "📚", "irving", 240, 180, "Midterm grind, 3rd floor", ["daniel", "haley"])
    a5 = create("haley", "Poker Night", "🃏", "koerner", 60 * 26, 180, "Buy-in is snacks", ["daniel", "shannon", "ethan"])

    # People join
    def join(user, act):
        if not act:
            return
        u, tok = accounts[user]
        call("PUT", f"activities/{act}/toggle-status/{u['id']}", token=tok)

    for u in ("haley", "ethan", "rafi"):
        join(u, a1)
    for u in ("daniel", "haley"):
        join(u, a2)
    join("ethan", a3)
    join("haley", a4)

    def chat(user, act, text):
        if not act:
            return
        u, tok = accounts[user]
        call("POST", "chat-messages", {"content": text, "senderUserId": u["id"], "activityId": act}, token=tok)

    chat("haley", a1, "Come grab dinner with us!! 🌯")
    chat("ethan", a1, "omw, 10 min")
    chat("daniel", a1, "grabbing a big table by the window")
    chat("rafi", a2, "who's bringing the cue chalk")
    chat("ethan", a3, "need 2 more for full court")
    print("seeded activities:", [a1, a2, a3, a4, a5])


if __name__ == "__main__":
    main()
