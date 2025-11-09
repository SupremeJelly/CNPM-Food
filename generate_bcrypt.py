import bcrypt

# Generate BCrypt hash for passwords
password1 = "111111"
password2 = "222222"

hash1 = bcrypt.hashpw(password1.encode('utf-8'), bcrypt.gensalt(rounds=10))
hash2 = bcrypt.hashpw(password2.encode('utf-8'), bcrypt.gensalt(rounds=10))

print(f"Password: {password1}")
print(f"BCrypt hash: {hash1.decode('utf-8')}")
print()
print(f"Password: {password2}")
print(f"BCrypt hash: {hash2.decode('utf-8')}")
