from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from utils.database import get_db
from models.models import ChangeLog
from datetime import datetime

router = APIRouter()

@router.get("/changes/")
def get_changes(since: str, db: Session = Depends(get_db)):

    try:
        since_dt = datetime.fromisoformat(since)
    except:
        return {"error": "Invalid timestamp format"}

    changes = db.query(ChangeLog).filter(
        ChangeLog.updatedAt > since_dt
    ).order_by(ChangeLog.updatedAt.asc()).all()

    if not changes:
        return {
            "changes": [],
            "count": 0,
            "latestTimestamp": since  # no new changes
        }

    latest_timestamp = max(c.updatedAt for c in changes)
    latest_changes = {}

    for change in changes:
        key = (change.entityType, change.entityID)

        if key not in latest_changes or latest_changes[key].updatedAt < change.updatedAt:
            latest_changes[key] = change

    result = sorted(
        latest_changes.values(),
        key=lambda c: c.updatedAt
    )

    return {
        "changes": [
            {
                "entityType": c.entityType,
                "entityID": c.entityID,
                "operationType": c.operationType,
                "updatedAt": c.updatedAt,
                "updatedBy": c.updatedBy
            }
            for c in result
        ],
        "count": len(result),
        "latestTimestamp": latest_timestamp
    }