#!/usr/bin/env python
import bcrypt
import argparse

def hash_password(password: str, rounds: int = 12) -> str:
    salt = bcrypt.gensalt(rounds)
    hashed = bcrypt.hashpw(password.encode('utf-8'), salt)
    return hashed.decode('utf-8')

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="BCrypt Password Hasher")
    parser.add_argument("password", help="Password to hash")
    parser.add_argument("-r", "--rounds", type=int, default=12, help="BCrypt rounds (cost factor, default: 12)")

    args = parser.parse_args()
    hashed = hash_password(args.password, args.rounds)

    print("BCrypt hash:")
    print(hashed)
