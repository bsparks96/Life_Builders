from models.models import ChangeLog
from datetime import datetime

def log_change(db, entityType: str, entityID: int, operationType: str = "UPDATE", userID: int = None):
    change = ChangeLog(
        entityType=entityType,
        entityID=entityID,
        operationType=operationType,  # must match ENUM: INSERT, UPDATE, DELETE
        updatedBy=userID,
        updatedAt=datetime.utcnow()
    )
    db.add(change)