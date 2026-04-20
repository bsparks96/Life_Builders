from pydantic import BaseModel
from typing import List, Optional

class UserBasicOut(BaseModel):
    userID: int
    username: str
    firstName: str | None
    lastName: str | None
    userRole: str

    class Config:
        from_attributes = True


class UserBulkRequest(BaseModel):
    userIDs: List[int]

class UserOut(BaseModel):
    userID: int
    firstName: str
    lastName: str

    class Config:
        from_attributes = True

class UserCreate(BaseModel):
    username: str
    password: str
    firstName: str
    lastName: str
    userRole: str

class UserLogin(BaseModel):
    username: str
    password: str

class UserUpdateRequest(BaseModel):
    userID: int
    username: Optional[str] = None
    firstName: Optional[str] = None
    lastName: Optional[str] = None
    userRole: Optional[str] = None
    newUserID: Optional[int] = None

class PasswordResetRequest(BaseModel):
    userID: int

class ChangePasswordRequest(BaseModel):
    currentPassword: str
    newPassword: str