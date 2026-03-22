from pydantic import BaseModel

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