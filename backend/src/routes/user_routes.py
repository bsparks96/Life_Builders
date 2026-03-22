from fastapi import Depends, APIRouter
from sqlalchemy.orm import Session
from models.models import User
from utils.database import get_db
from classes.user import UserOut

router = APIRouter()

@router.get("/users", response_model=list[UserOut])
def get_users(db: Session = Depends(get_db)):
    users = db.query(User).all()
    return users

