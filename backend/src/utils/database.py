from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, Session, declarative_base
import os
from dotenv import load_dotenv

load_dotenv()

DB_URL = os.getenv("DB_URL")  # e.g. mysql+mysqlconnector://user:pass@localhost:3306/lifebuilders

try:
    engine = create_engine(DB_URL)
    SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
    Base = declarative_base()
except Exception as e:
    print(f"Failed to connect to DB: {e}")


def get_db():
    db: Session = SessionLocal()
    try:
        yield db
    finally:
        db.close()