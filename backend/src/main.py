from fastapi import FastAPI
from utils.database import Base, engine
from models import models
from routes import user_routes, clientRoute, courseRoute, statisticsRoute
import traceback
app = FastAPI()
Base.metadata.create_all(bind=engine)

app.include_router(user_routes.router, prefix="/api")
app.include_router(courseRoute.router, prefix="/api", tags=["Courses"])
app.include_router(clientRoute.router, prefix="/api", tags=["Clients"])
app.include_router(user_routes.router, prefix="/api", tags=["Users"])

app.include_router(statisticsRoute.router, prefix="/api", tags=["Statistics"])
@app.get("/")
def root():
    return {"message": "LifeBuilders API is live!"}

###import logging
###logging.basicConfig(level=logging.INFO)
###logging.info("main.py loaded successfully")
###for route in app.routes:
###   print(f"Route: {route.path} → methods={route.methods}")