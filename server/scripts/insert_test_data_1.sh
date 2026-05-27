#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

API_URL="http://localhost:8000"
HASHED_PASSWORD="0b14d501a594442a01c6859541bcb3e8164d183d32937b851835442f69d5c94e"

echo -e "\n${YELLOW}[1/11] Resetting database...${NC}"
RESET_RESPONSE=$(curl -s -X POST "${API_URL}/admin/reset")
echo "$RESET_RESPONSE" | jq '.' 2>/dev/null || echo -e "${GREEN}Reset: $RESET_RESPONSE${NC}"
sleep 2

echo -e "\n${YELLOW}[2/11] Creating roles...${NC}"

ROLES=(
  '{"name":"admin","description":"Administrator with full access"}'
  '{"name":"supervisor","description":"Practice supervisor/teacher"}'
  '{"name":"student","description":"Regular student"}'
)

for role_json in "${ROLES[@]}"; do
  RESPONSE=$(curl -s -X POST "${API_URL}/roles" \
    -H "accept: application/json" \
    -H "Content-Type: application/json" \
    -d "$role_json")
  echo "$RESPONSE" | jq '.'
done

echo -e "${GREEN}Roles created.${NC}"

echo -e "\n${YELLOW}[3/11] Adding academic year 2025/2026...${NC}"
YEAR_RESPONSE=$(curl -s -X POST "${API_URL}/years" \
  -H "accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "2025/2026",
    "start_date": "2025-09-01",
    "end_date": "2026-06-30"
  }')
echo "$YEAR_RESPONSE" | jq '.'
YEAR_ID=$(echo "$YEAR_RESPONSE" | jq -r '.id // empty')

if [ -z "$YEAR_ID" ] || [ "$YEAR_ID" = "null" ]; then
  echo -e "${RED}Failed to create year. Exiting.${NC}"
  exit 1
fi
echo -e "${GREEN}Year ID: $YEAR_ID${NC}"

echo -e "\n${YELLOW}[4/11] Adding courses...${NC}"

COURSE_IDS=()
COURSE_NAMES=(
  "Программная инженерия"
  "Информационные системы"
  "Искусственный интеллект"
  "Кибербезопасность"
)

for i in "${!COURSE_NAMES[@]}"; do
  RESPONSE=$(curl -s -X POST "${API_URL}/courses" \
    -H "Content-Type: application/json" \
    -d "{\"name\": \"${COURSE_NAMES[$i]}\"}")
  echo "$RESPONSE" | jq '.'
  COURSE_IDS[$i]=$(echo "$RESPONSE" | jq -r '.id // empty')

  if [ -z "${COURSE_IDS[$i]}" ] || [ "${COURSE_IDS[$i]}" = "null" ]; then
    echo -e "${RED}Failed to create course: ${COURSE_NAMES[$i]}${NC}"
    exit 1
  fi
done

echo -e "${GREEN}Course IDs: ${COURSE_IDS[@]}${NC}"

echo -e "\n${YELLOW}[5/11] Adding groups...${NC}"

declare -A GROUP_IDS

GROUPS_DATA=(
  "ПИ-101:${COURSE_IDS[0]}"
  "ПИ-102:${COURSE_IDS[0]}"
  "ИС-201:${COURSE_IDS[1]}"
  "ИС-202:${COURSE_IDS[1]}"
  "ИИ-301:${COURSE_IDS[2]}"
)

for group_data in "${GROUPS_DATA[@]}"; do
  IFS=':' read -r group_name course_id <<< "$group_data"
  RESPONSE=$(curl -s -X POST "${API_URL}/groups" \
    -H "Content-Type: application/json" \
    -d "{
      \"name\": \"$group_name\",
      \"year_id\": $YEAR_ID,
      \"course_id\": $course_id
    }")
  echo "$RESPONSE" | jq '.'
  GROUP_IDS["$group_name"]=$(echo "$RESPONSE" | jq -r '.id // empty')

  if [ -z "${GROUP_IDS[$group_name]}" ] || [ "${GROUP_IDS[$group_name]}" = "null" ]; then
    echo -e "${RED}Failed to create group: $group_name${NC}"
    exit 1
  fi
done

echo -e "${GREEN}Groups added: ${#GROUP_IDS[@]}${NC}"

echo -e "\n${YELLOW}[6/11] Adding supervisors...${NC}"

declare -A SUPERVISOR_IDS

SUPERVISORS=(
  "andreev_ki:k.andreev@uni.edu:Андреев К.И."
  "belova_ma:m.belova@uni.edu:Белова М.А."
  "vasiliev_po:p.vasiliev@uni.edu:Васильев П.О."
  "dmitriev_as:a.dmitriev@uni.edu:Дмитриев А.С."
  "ermakova_ov:o.ermakova@uni.edu:Ермакова О.В."
)

for supervisor in "${SUPERVISORS[@]}"; do
  IFS=':' read -r username email fullname <<< "$supervisor"
  RESPONSE=$(curl -s -X POST "${API_URL}/users/register" \
    -H "accept: application/json" \
    -H "Content-Type: application/json" \
    -d "{
      \"username\": \"$username\",
      \"email\": \"$email\",
      \"password\": \"$HASHED_PASSWORD\",
      \"roles\": [\"supervisor\"]
    }")
  echo "$RESPONSE" | jq '.'
  SUPERVISOR_IDS["$fullname"]=$(echo "$RESPONSE" | jq -r '.id // empty')

  if [ -z "${SUPERVISOR_IDS[$fullname]}" ] || [ "${SUPERVISOR_IDS[$fullname]}" = "null" ]; then
    echo -e "${RED}Failed to create supervisor: $fullname${NC}"
    exit 1
  fi
done

echo -e "${GREEN}Supervisors added: ${#SUPERVISOR_IDS[@]}${NC}"

echo -e "\n${YELLOW}[7/11] Adding students...${NC}"

declare -A STUDENT_IDS

STUDENTS=(
  "abramov_vk:Абрамов В.К.:ПИ-101"
  "blinov_ap:Блинов А.П.:ПИ-101"
  "vlasov_de:Власов Д.Е.:ПИ-101"
  "gusev_im:Гусев И.М.:ПИ-101"
  "drozdov_ns:Дроздов Н.С.:ПИ-101"
  "kabanov_oi:Кабанов О.И.:ПИ-102"
  "larin_vp:Ларин В.П.:ПИ-102"
  "martynov_es:Мартынов Е.С.:ПИ-102"
  "tarasova_ev:Тарасова Е.В.:ИС-201"
  "ustinova_as:Устинова А.С.:ИС-201"
  "filippov_dm:Филиппов Д.М.:ИС-201"
  "yudina_os:Юдина О.С.:ИИ-301"
  "yakovlev_ip:Яковлев И.П.:ИИ-301"
  "andreeva_kv:Андреева К.В.:ИИ-301"
)

for student in "${STUDENTS[@]}"; do
  IFS=':' read -r username fullname group <<< "$student"
  RESPONSE=$(curl -s -X POST "${API_URL}/users/register" \
    -H "accept: application/json" \
    -H "Content-Type: application/json" \
    -d "{
      \"username\": \"$username\",
      \"email\": \"${username}@student.edu\",
      \"password\": \"$HASHED_PASSWORD\",
      \"roles\": [\"student\"]
    }")
  STUDENT_IDS["$fullname"]=$(echo "$RESPONSE" | jq -r '.id // empty')

  if [ -z "${STUDENT_IDS[$fullname]}" ] || [ "${STUDENT_IDS[$fullname]}" = "null" ]; then
    echo -e "${RED}Failed to create student: $fullname${NC}"
    exit 1
  fi

  curl -s -X POST "${API_URL}/student-groups" \
    -H "accept: application/json" \
    -H "Content-Type: application/json" \
    -d "{
      \"student_id\": ${STUDENT_IDS["$fullname"]},
      \"group_id\": ${GROUP_IDS[$group]}
    }" > /dev/null
  echo "  Added: $fullname -> $group (ID: ${STUDENT_IDS[$fullname]})"
done

echo -e "${GREEN}Students added: ${#STUDENT_IDS[@]}${NC}"

echo -e "\n${YELLOW}[8/11] Adding practice bases...${NC}"

declare -A BASE_IDS

BASES=(
  "ТехноИнновации:Разработка инновационных IT-решений:15:${COURSE_IDS[0]}:null:Андреев К.И."
  "СофтЛаб:Лаборатория разработки ПО:10:${COURSE_IDS[0]}:${GROUP_IDS["ПИ-101"]}:Белова М.А."
  "ИнфоСистемы:Внедрение информационных систем:12:${COURSE_IDS[1]}:null:Васильев П.О."
  "НейроТехнологии:Исследования в области нейросетей:8:${COURSE_IDS[2]}:${GROUP_IDS["ИИ-301"]}:Дмитриев А.С."
)

for base in "${BASES[@]}"; do
  IFS=':' read -r name description capacity course_id group_id supervisor <<< "$base"
  RESPONSE=$(curl -s -X POST "${API_URL}/practice" \
    -H "accept: application/json" \
    -H "Content-Type: application/json" \
    -d "{
      \"name\": \"$name\",
      \"description\": \"$description\",
      \"capacity\": $capacity,
      \"year_id\": $YEAR_ID,
      \"course_id\": $course_id,
      \"group_id\": $group_id,
      \"supervisor_id\": ${SUPERVISOR_IDS["$supervisor"]}
    }")
  echo "$RESPONSE" | jq '.'
  BASE_IDS["$name"]=$(echo "$RESPONSE" | jq -r '.id // empty')

  if [ -z "${BASE_IDS[$name]}" ] || [ "${BASE_IDS[$name]}" = "null" ]; then
    echo -e "${RED}Failed to create practice base: $name${NC}"
    exit 1
  fi
done

echo -e "${GREEN}Practice bases added: ${#BASE_IDS[@]}${NC}"

echo -e "\n${YELLOW}[9/11] Registering students for practice...${NC}"

REGISTRATIONS=(
  "Абрамов В.К.:ТехноИнновации"
  "Блинов А.П.:СофтЛаб"
  "Власов Д.Е.:ТехноИнновации"
  "Гусев И.М.:СофтЛаб"
  "Кабанов О.И.:ТехноИнновации"
  "Ларин В.П.:СофтЛаб"
  "Тарасова Е.В.:ИнфоСистемы"
  "Устинова А.С.:ИнфоСистемы"
  "Юдина О.С.:НейроТехнологии"
  "Яковлев И.П.:НейроТехнологии"
)

for reg in "${REGISTRATIONS[@]}"; do
  IFS=':' read -r student_name base_name <<< "$reg"
  curl -s -X POST "${API_URL}/practice/register" \
    -H "accept: application/json" \
    -H "Content-Type: application/json" \
    -d "{
      \"user_id\": ${STUDENT_IDS["$student_name"]},
      \"base_id\": ${BASE_IDS["$base_name"]}
    }" > /dev/null
  echo "  Registered: $student_name -> $base_name"
done

echo -e "\n${YELLOW}[10/11] Checking chats...${NC}"

GENERAL_CHAT_RESPONSE=$(curl -s "${API_URL}/chats?user_id=${SUPERVISOR_IDS["Андреев К.И."]}")
GENERAL_CHAT_ID=$(echo "$GENERAL_CHAT_RESPONSE" | jq -r '.chats[]? | select(.name=="Общий чат") | .chat_id // empty')

if [ -z "$GENERAL_CHAT_ID" ]; then
  echo -e "${RED}General chat not found, creating...${NC}"
  CREATE_RESPONSE=$(curl -s -X POST "${API_URL}/chats" \
    -H "accept: application/json" \
    -H "Content-Type: application/json" \
    -d "{
      \"name\": \"Общий чат\",
      \"user_ids\": [${SUPERVISOR_IDS["Андреев К.И."]}, ${SUPERVISOR_IDS["Васильев П.О."]}]
    }")
  GENERAL_CHAT_ID=$(echo "$CREATE_RESPONSE" | jq -r '.chat_id // empty')
  echo "Created general chat ID: $GENERAL_CHAT_ID"
else
  echo -e "${GREEN}General chat ID: $GENERAL_CHAT_ID${NC}"
fi

echo -e "\n${YELLOW}[11/11] Adding messages...${NC}"

if [ -n "$GENERAL_CHAT_ID" ] && [ "$GENERAL_CHAT_ID" != "null" ]; then
  MESSAGES=(
    "${SUPERVISOR_IDS["Андреев К.И."]}:Уважаемые студенты! Начинается практика 2025/2026 года."
    "${SUPERVISOR_IDS["Васильев П.О."]}:Прошу всех внимательно ознакомиться с программой практики."
    "${STUDENT_IDS["Абрамов В.К."]}:Спасибо! Когда будут распределены руководители?"
    "${SUPERVISOR_IDS["Дмитриев А.С."]}:Распределение уже готово, проверьте свои чаты практик."
  )

  for msg in "${MESSAGES[@]}"; do
    IFS=':' read -r sender_id content <<< "$msg"
    curl -s -X POST "${API_URL}/messages" \
      -H "accept: application/json" \
      -H "Content-Type: application/json" \
      -d "{
        \"chat_id\": $GENERAL_CHAT_ID,
        \"sender_id\": $sender_id,
        \"content\": \"$content\"
      }" > /dev/null
    echo "  ✓ Message in general chat from ID: $sender_id"
  done
fi

echo "Fetching practice chats..."
ALL_CHATS=$(curl -s "${API_URL}/chats/all")

for base_name in "${!BASE_IDS[@]}"; do
  CHAT_NAME="Чат практики: ${base_name}"
  CHAT_ID=$(echo "$ALL_CHATS" | jq -r --arg name "$CHAT_NAME" '.[]? | select(.name==$name) | .chat_id // empty')

  if [ -z "$CHAT_ID" ]; then
    echo "  Chat for $base_name not found, skipping..."
    continue
  fi

  case $base_name in
    "ТехноИнновации")
      curl -s -X POST "${API_URL}/messages" \
        -H "accept: application/json" \
        -H "Content-Type: application/json" \
        -d "{
          \"chat_id\": $CHAT_ID,
          \"sender_id\": ${SUPERVISOR_IDS["Андреев К.И."]},
          \"content\": \"Добро пожаловать в команду ТехноИнновации! Начинаем работу над проектами.\"
        }" > /dev/null

      curl -s -X POST "${API_URL}/messages" \
        -H "accept: application/json" \
        -H "Content-Type: application/json" \
        -d "{
          \"chat_id\": $CHAT_ID,
          \"sender_id\": ${STUDENT_IDS["Власов Д.Е."]},
          \"content\": \"Здравствуйте! Готовы приступить к заданиям.\"
        }" > /dev/null
      ;;
    "СофтЛаб")
      curl -s -X POST "${API_URL}/messages" \
        -H "accept: application/json" \
        -H "Content-Type: application/json" \
        -d "{
          \"chat_id\": $CHAT_ID,
          \"sender_id\": ${SUPERVISOR_IDS["Белова М.А."]},
          \"content\": \"Коллеги, начинаем работу над проектом 'Умный дом'. Жду предложений.\"
        }" > /dev/null
      ;;
    "ИнфоСистемы")
      curl -s -X POST "${API_URL}/messages" \
        -H "accept: application/json" \
        -H "Content-Type: application/json" \
        -d "{
          \"chat_id\": $CHAT_ID,
          \"sender_id\": ${SUPERVISOR_IDS["Васильев П.О."]},
          \"content\": \"Завтра в 10:00 встреча по проекту ERP-системы.\"
        }" > /dev/null

      curl -s -X POST "${API_URL}/messages" \
        -H "accept: application/json" \
        -H "Content-Type: application/json" \
        -d "{
          \"chat_id\": $CHAT_ID,
          \"sender_id\": ${STUDENT_IDS["Тарасова Е.В."]},
          \"content\": \"Буду вовремя, подготовлю вопросы по архитектуре.\"
        }" > /dev/null
      ;;
    "НейроТехнологии")
      curl -s -X POST "${API_URL}/messages" \
        -H "accept: application/json" \
        -H "Content-Type: application/json" \
        -d "{
          \"chat_id\": $CHAT_ID,
          \"sender_id\": ${SUPERVISOR_IDS["Дмитриев А.С."]},
          \"content\": \"Начинаем исследования в области компьютерного зрения. Изучите материалы.\"
        }" > /dev/null
      ;;
  esac
  echo "  ✓ Messages added to chat: $base_name (ID: $CHAT_ID)"
done

echo -e "\n${BLUE}========================================${NC}"
echo -e "${GREEN}✅ TEST DATA INSERTION COMPLETED!${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "${YELLOW}Statistics:${NC}"
echo -e "  👑 Roles: 3 (admin, supervisor, student)"
echo -e "  📅 Academic year: 2025/2026 (ID: $YEAR_ID)"
echo -e "  📚 Courses: ${#COURSE_IDS[@]}"
echo -e "  👥 Groups: ${#GROUP_IDS[@]}"
echo -e "  👨‍🏫 Supervisors: ${#SUPERVISOR_IDS[@]}"
echo -e "  👨‍🎓 Students: ${#STUDENT_IDS[@]}"
echo -e "  🏢 Practice bases: ${#BASE_IDS[@]}"
echo -e "  📝 Registrations: ${#REGISTRATIONS[@]}"
echo -e "  👥 Total users: $((${#STUDENT_IDS[@]} + ${#SUPERVISOR_IDS[@]}))"
echo -e "${BLUE}========================================${NC}"
