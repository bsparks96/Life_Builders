------------------------------------------------------------
Install Instructions:
 - Navigate to the `/src` directory in your terminal
 - pip install -r requirements.txt
 - uvicorn main:app --reload
 - http://localhost:8000/docs
------------------------------------------------------------

/classes
Utilizes Pydantic to define data validation and serialization models for API request and response bodies.
These classes ensure that incoming API data is properly formatted and validated before being processed or stored.

------------------------------------------------------------

/models
Utilizes SQLAlchemy ORM to define all database table structures and relationships.
Each class in this folder corresponds to a table in the MySQL database and handles how data is persisted.

------------------------------------------------------------

/routes
Defines the API endpoints for the application using FastAPI's APIRouter.
Each file represents a different domain (e.g., user, student, courses) and includes the logic for handling HTTP requests such as GET, POST, PUT, and DELETE.

------------------------------------------------------------

/utils
Contains helper functions, shared logic, and utility classes.
This includes things like database session creation, hashing functions, date formatting, or environment variable loading

