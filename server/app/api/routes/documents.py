from fastapi import APIRouter, Depends, HTTPException, UploadFile, File, Form, status
import os
import shutil

from data_base import Database, MyDatabaseError

router = APIRouter()

UPLOAD_DIR = "uploads/documents"
os.makedirs(UPLOAD_DIR, exist_ok=True)


async def get_documents_repo():
    async with Database() as db:
        yield db.get_repository("documents")


@router.post("/upload", status_code=status.HTTP_201_CREATED)
async def upload_document(
    practice_base_id: int = Form(...),
    user_id: int = Form(...),
    document_type: str = Form(...),
    file: UploadFile = File(...),
    documents_repo=Depends(get_documents_repo)
):
    try:
        practice_dir = os.path.join(UPLOAD_DIR, str(practice_base_id))
        os.makedirs(practice_dir, exist_ok=True)

        ext = os.path.splitext(file.filename)[1].lower() or ".pdf"
        safe_filename = f"{document_type}_{user_id}{ext}"
        file_path = os.path.join(practice_dir, safe_filename)

        with open(file_path, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)

        await documents_repo.insert((
            practice_base_id,
            user_id,
            document_type,
            safe_filename,
            file_path
        ))

        return {
            "success": True,
            "message": "Документ успешно загружен",
            "file_name": safe_filename
        }

    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Upload error: {str(e)}")

@router.get("/user/{user_id}")
async def get_user_documents(
    user_id: int,
    documents_repo=Depends(get_documents_repo)
):
    try:
        docs = await documents_repo.fetch_many("by_user", (user_id,))
        return {"documents": docs}
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")
