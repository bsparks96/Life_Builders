from fastapi import Depends, APIRouter, HTTPException
from sqlalchemy.orm import Session
from models.models import User
from utils.database import get_db
from classes.user import UserOut, UserCreate, UserLogin, UserBasicOut, UserBulkRequest, UserUpdateRequest, PasswordResetRequest, ChangePasswordRequest
from utils.security import hash_password, verify_password, create_access_token, get_current_user, require_admin
from passlib.context import CryptContext
import secrets, string

from utils.change_logger import log_change

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

@router.post("/users/create/")
def create_user(
        user: UserCreate,
        db: Session = Depends(get_db),
        current_user: dict = Depends(require_admin)
):
    existing_user = get_user_by_username(db, user.username)
    if existing_user:
        raise HTTPException(status_code=400, detail="Username already exists")

    hashed_password = hash_password(user.password)

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

    current_user = {
    "userID": 10001
    }

    log_change(db, "User", new_user.userID, "INSERT", current_user["userID"])

    db.commit()

    return {
        "message": "User created successfully",
        "userID": new_user.userID
    }

@router.post("/users/login")
def login(user: UserLogin, db: Session = Depends(get_db)):
    print("LOGIN HIT:", user.username)
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

    if db_user.mustChangePassword:
        return {
            "access_token": access_token,
            "token_type": "bearer",
            "mustChangePassword": True
        }

    # 4. Return token
    return {
        "access_token": access_token,
        "token_type": "bearer"
    }

@router.get("/users/me")
def get_current_user_info(current_user: dict = Depends(get_current_user)):
    return current_user


@router.post("/users/bulk/", response_model=list[UserBasicOut])
def get_users_by_ids(request: UserBulkRequest, db: Session = Depends(get_db)):

    if not request.userIDs:
        return []

    users = db.query(User).filter(User.userID.in_(request.userIDs)).all()

    if not users:
        raise HTTPException(status_code=404, detail="No users found")

    return users


@router.put("/users/update/")
def update_user(request: UserUpdateRequest, db: Session = Depends(get_db)):

    try:
        # 🔹 1. Find existing user
        user = db.query(User).filter(User.userID == request.userID).first()

        if not user:
            raise HTTPException(status_code=404, detail="User not found")

        # 🔹 2. Validate username uniqueness
        if request.username:
            existing_username = db.query(User).filter(
                User.username == request.username,
                User.userID != request.userID
            ).first()

            if existing_username:
                raise HTTPException(
                    status_code=400,
                    detail="Username already in use"
                )

        # 🔹 3. Validate new userID uniqueness (if provided)
        if request.newUserID:
            existing_id = db.query(User).filter(
                User.userID == request.newUserID
            ).first()

            if existing_id:
                raise HTTPException(
                    status_code=400,
                    detail="UserID already in use"
                )

        # 🔹 4. Apply updates (ONLY if provided)
        if request.username is not None:
            user.username = request.username

        if request.firstName is not None:
            user.firstName = request.firstName

        if request.lastName is not None:
            user.lastName = request.lastName

        if request.userRole is not None:
            user.userRole = request.userRole



        # 🔹 5. Commit transaction
        db.commit()
        db.refresh(user)

        current_user = {
            "userID": 10001
        }

        log_change(db, "User", user.userID, "UPDATE", current_user["userID"])

        db.commit()

        return {
            "message": "User updated successfully",
            "user": {
                "userID": user.userID,
                "username": user.username,
                "firstName": user.firstName,
                "lastName": user.lastName,
                "userRole": user.userRole
            }
        }

    except HTTPException:
        db.rollback()
        raise

    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=str(e))

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


def generate_temp_password(length=10):
    alphabet = string.ascii_letters + string.digits
    return ''.join(secrets.choice(alphabet) for _ in range(length))


@router.post("/users/reset-password/")
def reset_password(request: PasswordResetRequest, db: Session = Depends(get_db), current_user=Depends(require_admin)):



    try:
        user = db.query(User).filter(User.userID == request.userID).first()

        if not user:
            raise HTTPException(status_code=404, detail="User not found")

        # Generate secure temp password
        temp_password = generate_temp_password()

        # Hash it
        user.passwordHash = pwd_context.hash(temp_password)

        # Force password change on next login
        user.mustChangePassword = True

        db.commit()

        return {
            "message": "Password reset successfully",
            "temporaryPassword": temp_password
        }

    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=str(e))

@router.put("/users/change-password/")
def change_password(request: ChangePasswordRequest, db: Session = Depends(get_db), current_user=Depends(get_current_user)):

    user = db.query(User).filter(User.userID == current_user["userID"]).first()

    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    if not verify_password(request.currentPassword, user.passwordHash):
        raise HTTPException(status_code=401, detail="Current password incorrect")

    user.passwordHash = pwd_context.hash(request.newPassword)
    user.mustChangePassword = False

    db.commit()

    return {"message": "Password updated successfully"}