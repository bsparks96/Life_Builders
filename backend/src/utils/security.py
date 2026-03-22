from passlib.context import CryptContext
from datetime import datetime, timedelta
from jose import jwt, JWTError
from fastapi import Depends, HTTPException
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
import os

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

def hash_password(password: str):
    return pwd_context.hash(password)

def verify_password(plain_password: str, hashed_password: str):
    return pwd_context.verify(plain_password, hashed_password)

# ===== JWT Config =====
SECRET_KEY = os.getenv("SECRET_KEY")
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 60


# ===== Create JWT =====
def create_access_token(data: dict):
    to_encode = data.copy()
    expire = datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)


# ===== Decode JWT =====
def decode_access_token(token: str):
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        return payload
    except JWTError:
        return None


security = HTTPBearer()

def get_current_user(token: HTTPAuthorizationCredentials = Depends(security)):
    payload = decode_access_token(token.credentials)

    if payload is None:
        raise HTTPException(status_code=401, detail="Could not validate credentials")

    return payload

def require_admin(current_user: dict = Depends(get_current_user)):
    #print("Current user role: ", current_user.get("role"))
    if current_user.get("role").lower() != "admin":
        raise HTTPException(status_code=403, detail="Admin permissions required")
    return current_user