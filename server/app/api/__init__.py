from fastapi import APIRouter
from .routes import (
    reset,
    users,
    years,
    courses,
    groups,
    practice,
    student_groups,
    practice_registrations,
    chats,
    chat_members,
    messages,
    roles
)

router = APIRouter()

router.include_router(reset.router, prefix="/admin", tags=["Admin"])
router.include_router(users.router, prefix="/users", tags=["Users"])
router.include_router(years.router, prefix="/years", tags=["Years"])
router.include_router(courses.router, prefix="/courses", tags=["Courses"])
router.include_router(roles.router, prefix="/roles", tags=["Roles"])
router.include_router(groups.router, prefix="/groups", tags=["Groups"])
router.include_router(practice.router, prefix="/practice", tags=["Practice"])
router.include_router(student_groups.router, prefix="/student-groups", tags=["Student Groups"])
router.include_router(practice_registrations.router, prefix="/practice", tags=["Practice Registrations"])
router.include_router(chats.router, prefix="/chats", tags=["Chats"])
router.include_router(chat_members.router, prefix="/chat-members", tags=["Chat Members"])
router.include_router(messages.router, prefix="/messages", tags=["Messages"])
