from fastapi import Depends, APIRouter, HTTPException
from sqlalchemy.orm import Session
from models.models import User
from utils.database import get_db
from classes.user import UserOut, UserCreate, UserLogin
from utils.security import hash_password, verify_password, create_access_token, get_current_user, require_admin

router = APIRouter()

def get_user_by_username(db: Session, username: str):
    return db.query(User).filter(User.username == username).first()

@router.get("/users", response_model=list[UserOut])
def get_users(
        db: Session = Depends(get_db),
        current_user: dict = Depends(get_current_user)
):
    users = db.query(User).all()
    return users

@router.post("/users/create")
def create_user(
        user: UserCreate,
        db: Session = Depends(get_db),
        current_user: dict = Depends(require_admin)
):
    # Check if user already exists
    existing_user = get_user_by_username(db, user.username)
    if existing_user:
        raise HTTPException(status_code=400, detail="Username already exists")

    # Hash password
    hashed_password = hash_password(user.password)

    # Create user
    new_user = User(
        username=user.username,
        passwordHash=hashed_password,
        firstName=user.firstName,
        lastName=user.lastName,
        userRole=user.userRole
    )

    db.add(new_user)
    db.commit()
    db.refresh(new_user)

    return {
        "message": "User created successfully",
        "userID": new_user.userID
    }

@router.post("/users/login")
def login(user: UserLogin, db: Session = Depends(get_db)):
    # 1. Lookup user
    db_user = get_user_by_username(db, user.username)
    if not db_user:
        raise HTTPException(status_code=401, detail="Invalid username or password")

    # 2. Verify password
    if not verify_password(user.password, db_user.passwordHash):
        raise HTTPException(status_code=401, detail="Invalid username or password")

    # 3. Create JWT token
    token_data = {
        "sub": db_user.username,
        "userID": db_user.userID,
        "role": db_user.userRole
    }
    print("Current user role: ", token_data)
    access_token = create_access_token(token_data)

    # 4. Return token
    return {
        "access_token": access_token,
        "token_type": "bearer"
    }

@router.get("/users/me")
def get_current_user_info(current_user: dict = Depends(get_current_user)):
    return current_user