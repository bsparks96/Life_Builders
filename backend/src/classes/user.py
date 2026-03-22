from pydantic import BaseModel

class UserOut(BaseModel):
    userID: int
    firstName: str
    lastName: str

    class Config:
        orm_mode = True