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
  "ПИ-103:${COURSE_IDS[0]}"
  "ИС-201:${COURSE_IDS[1]}"
  "ИС-202:${COURSE_IDS[1]}"
  "ИИ-301:${COURSE_IDS[2]}"
  "ИИ-302:${COURSE_IDS[2]}"
  "КБ-401:${COURSE_IDS[3]}"
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
  "zhukov_vn:v.zhukov@uni.edu:Жуков В.Н."
  "zaitseva_ta:t.zaitseva@uni.edu:Зайцева Т.А."
  "ivanov_sg:s.ivanov@uni.edu:Иванов С.Г."
  "kuznetsova_ev:e.kuznetsova@uni.edu:Кузнецова Е.В."
  "sokolov_ma:m.sokolov@uni.edu:Соколов М.А."
  "novikova_lp:l.novikova@uni.edu:Новикова Л.П."
  "fedorov_av:a.fedorov@uni.edu:Федоров А.В."
)

for supervisor in "${SUPERVISORS[@]}"; do
  IFS=':' read -r username email fullname <<< "$supervisor"
  RESPONSE=$(curl -s -X POST "${API_URL}/users/register" \
    -H "accept: application/json" \
    -H "Content-Type: application/json" \
    -d "{
      \"username\": \"$fullname\",
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
  # ПИ-101 (5 студентов)
  "abramov_vk:Абрамов В.К.:ПИ-101"
  "blinov_ap:Блинов А.П.:ПИ-101"
  "vlasov_de:Власов Д.Е.:ПИ-101"
  "gusev_im:Гусев И.М.:ПИ-101"
  "drozdov_ns:Дроздов Н.С.:ПИ-101"
  # ПИ-102 (5 студентов)
  "kabanov_oi:Кабанов О.И.:ПИ-102"
  "larin_vp:Ларин В.П.:ПИ-102"
  "martynov_es:Мартынов Е.С.:ПИ-102"
  "nikolaeva_av:Николаева А.В.:ПИ-102"
  "orlov_ds:Орлов Д.С.:ПИ-102"
  # ПИ-103 (5 студентов)
  "pavlov_ki:Павлов К.И.:ПИ-103"
  "romanov_av:Романов А.В.:ПИ-103"
  "sidorov_mn:Сидоров М.Н.:ПИ-103"
  "titova_ev:Титова Е.В.:ПИ-103"
  "ushakov_po:Ушаков П.О.:ПИ-103"
  # ИС-201 (4 студента)
  "tarasova_ev:Тарасова Е.В.:ИС-201"
  "ustinova_as:Устинова А.С.:ИС-201"
  "filippov_dm:Филиппов Д.М.:ИС-201"
  "haritonova_on:Харитонова О.Н.:ИС-201"
  # ИС-202 (4 студента)
  "chernykh_ev:Черных Е.В.:ИС-202"
  "sharov_da:Шаров Д.А.:ИС-202"
  "shchukina_ma:Щукина М.А.:ИС-202"
  "yakovlev_ip:Яковлев И.П.:ИС-202"
  # ИИ-301 (4 студента)
  "andreeva_kv:Андреева К.В.:ИИ-301"
  "borisov_sm:Борисов С.М.:ИИ-301"
  "volkova_na:Волкова Н.А.:ИИ-301"
  "galkin_dv:Галкин Д.В.:ИИ-301"
  # ИИ-302 (4 студента)
  "ershov_iv:Ершов И.В.:ИИ-302"
  "zhdanova_ma:Жданова М.А.:ИИ-302"
  "zuev_ks:Зуев К.С.:ИИ-302"
  "isaeva_ov:Исаева О.В.:ИИ-302"
  # КБ-401 (4 студента)
  "lobanov_ai:Лобанов А.И.:КБ-401"
  "mironov_dv:Миронов Д.В.:КБ-401"
  "nazarova_ep:Назарова Е.П.:КБ-401"
  "ovchinnikov_rs:Овчинников Р.С.:КБ-401"
)

for student in "${STUDENTS[@]}"; do
  IFS=':' read -r username fullname group <<< "$student"
  RESPONSE=$(curl -s -X POST "${API_URL}/users/register" \
    -H "accept: application/json" \
    -H "Content-Type: application/json" \
    -d "{
      \"username\": \"$fullname\",
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

# ═══════════════════════════════════════════
# ПИ-101: 4 базы
# ПИ-102: 4 базы
# ПИ-103: 3 базы
# ИС-201: 4 базы
# ИС-202: 3 базы
# ИИ-301: 4 базы
# ИИ-302: 3 базы
# КБ-401: 3 базы
# Всего: 28 баз
# ═══════════════════════════════════════════

BASES=(
  # ── ПИ-101 ──
  "ТехноИнновации:Разработка инновационных IT-решений:8:${COURSE_IDS[0]}:${GROUP_IDS["ПИ-101"]}:Андреев К.И."
  "СофтЛаб:Лаборатория разработки ПО:6:${COURSE_IDS[0]}:${GROUP_IDS["ПИ-101"]}:Белова М.А."
  "КодМастер:Обучение и разработка на Python:5:${COURSE_IDS[0]}:${GROUP_IDS["ПИ-101"]}:Жуков В.Н."
  "ДевСтудио:Веб-разработка и дизайн:5:${COURSE_IDS[0]}:${GROUP_IDS["ПИ-101"]}:Зайцева Т.А."

  # ── ПИ-102 ──
  "ОблачныеРешения:Облачные технологии и DevOps:6:${COURSE_IDS[0]}:${GROUP_IDS["ПИ-102"]}:Иванов С.Г."
  "МикросервисАрх:Микросервисная архитектура:5:${COURSE_IDS[0]}:${GROUP_IDS["ПИ-102"]}:Кузнецова Е.В."
  "ТестировщикПлюс:Автоматизация тестирования ПО:5:${COURSE_IDS[0]}:${GROUP_IDS["ПИ-102"]}:Соколов М.А."
  "Киберфорум:Безопасная разработка:5:${COURSE_IDS[0]}:${GROUP_IDS["ПИ-102"]}:Федоров А.В."

  # ── ПИ-103 ──
  "МобилАпп:Разработка мобильных приложений:6:${COURSE_IDS[0]}:${GROUP_IDS["ПИ-103"]}:Новикова Л.П."
  "ГеймДевСтудия:Разработка игр на Unity:5:${COURSE_IDS[0]}:${GROUP_IDS["ПИ-103"]}:Зайцева Т.А."
  "UXЛаб:UX/UI исследования и дизайн:5:${COURSE_IDS[0]}:${GROUP_IDS["ПИ-103"]}:Белова М.А."

  # ── ИС-201 ──
  "ИнфоСистемы:Внедрение информационных систем:5:${COURSE_IDS[1]}:${GROUP_IDS["ИС-201"]}:Васильев П.О."
  "ERPЭксперт:Внедрение ERP-систем:4:${COURSE_IDS[1]}:${GROUP_IDS["ИС-201"]}:Соколов М.А."
  "БизнесАналитика:Анализ бизнес-процессов:4:${COURSE_IDS[1]}:${GROUP_IDS["ИС-201"]}:Новикова Л.П."
  "ДокументоОборот:Системы электронного документооборота:4:${COURSE_IDS[1]}:${GROUP_IDS["ИС-201"]}:Иванов С.Г."

  # ── ИС-202 ──
  "ДатаЦентр:Центр обработки данных:5:${COURSE_IDS[1]}:${GROUP_IDS["ИС-202"]}:Ермакова О.В."
  "СетевыеТехнологии:Проектирование сетевой инфраструктуры:4:${COURSE_IDS[1]}:${GROUP_IDS["ИС-202"]}:Федоров А.В."
  "ИТАутсорсинг:ИТ-аутсорсинг и поддержка:4:${COURSE_IDS[1]}:${GROUP_IDS["ИС-202"]}:Кузнецова Е.В."

  # ── ИИ-301 ──
  "НейроТехнологии:Исследования в области нейросетей:5:${COURSE_IDS[2]}:${GROUP_IDS["ИИ-301"]}:Дмитриев А.С."
  "МашинноеОбучение:Разработка ML-моделей:4:${COURSE_IDS[2]}:${GROUP_IDS["ИИ-301"]}:Жуков В.Н."
  "КомпьютерноеЗрение:Системы распознавания образов:4:${COURSE_IDS[2]}:${GROUP_IDS["ИИ-301"]}:Андреев К.И."
  "РоботоТех:Интеллектуальная робототехника:3:${COURSE_IDS[2]}:${GROUP_IDS["ИИ-301"]}:Соколов М.А."

  # ── ИИ-302 ──
  "АналитикаДанных:Анализ данных и предиктивная аналитика:5:${COURSE_IDS[2]}:${GROUP_IDS["ИИ-302"]}:Новикова Л.П."
  "БигДата:Обработка больших данных:4:${COURSE_IDS[2]}:${GROUP_IDS["ИИ-302"]}:Жуков В.Н."
  "НЛПЛаб:Обработка естественного языка:4:${COURSE_IDS[2]}:${GROUP_IDS["ИИ-302"]}:Дмитриев А.С."

  # ── КБ-401 ──
  "КиберЩит:Центр кибербезопасности:5:${COURSE_IDS[3]}:${GROUP_IDS["КБ-401"]}:Иванов С.Г."
  "ПентестЛаб:Тестирование на проникновение:4:${COURSE_IDS[3]}:${GROUP_IDS["КБ-401"]}:Федоров А.В."
  "Форензика:Цифровая криминалистика:4:${COURSE_IDS[3]}:${GROUP_IDS["КБ-401"]}:Ермакова О.В."
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
  # ── ПИ-101 (5 студентов на 4 базы) ──
  "Абрамов В.К.:ТехноИнновации"
  "Блинов А.П.:СофтЛаб"
  "Власов Д.Е.:ТехноИнновации"
  "Гусев И.М.:КодМастер"
  "Дроздов Н.С.:ДевСтудио"

  # ── ПИ-102 (5 студентов на 4 базы) ──
  "Кабанов О.И.:ОблачныеРешения"
  "Ларин В.П.:ОблачныеРешения"
  "Мартынов Е.С.:МикросервисАрх"
  "Николаева А.В.:ТестировщикПлюс"
  "Орлов Д.С.:Киберфорум"

  # ── ПИ-103 (5 студентов на 3 базы) ──
  "Павлов К.И.:МобилАпп"
  "Романов А.В.:МобилАпп"
  "Сидоров М.Н.:ГеймДевСтудия"
  "Титова Е.В.:ГеймДевСтудия"
  "Ушаков П.О.:UXЛаб"

  # ── ИС-201 (4 студента на 4 базы) ──
  "Тарасова Е.В.:ИнфоСистемы"
  "Устинова А.С.:ERPЭксперт"
  "Филиппов Д.М.:БизнесАналитика"
  "Харитонова О.Н.:ДокументоОборот"

  # ── ИС-202 (4 студента на 3 базы) ──
  "Черных Е.В.:ДатаЦентр"
  "Шаров Д.А.:ДатаЦентр"
  "Щукина М.А.:СетевыеТехнологии"
  "Яковлев И.П.:ИТАутсорсинг"

  # ── ИИ-301 (4 студента на 4 базы) ──
  "Андреева К.В.:НейроТехнологии"
  "Борисов С.М.:МашинноеОбучение"
  "Волкова Н.А.:КомпьютерноеЗрение"
  "Галкин Д.В.:РоботоТех"

  # ── ИИ-302 (4 студента на 3 базы) ──
  "Ершов И.В.:АналитикаДанных"
  "Жданова М.А.:АналитикаДанных"
  "Зуев К.С.:БигДата"
  "Исаева О.В.:НЛПЛаб"

  # ── КБ-401 (4 студента на 3 базы) ──
  "Лобанов А.И.:КиберЩит"
  "Миронов Д.В.:КиберЩит"
  "Назарова Е.П.:ПентестЛаб"
  "Овчинников Р.С.:Форензика"
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
    "${SUPERVISOR_IDS["Андреев К.И."]}:Уважаемые студенты! Начинается практика 2025/2026 года. Проверьте свои группы в чатах."
    "${SUPERVISOR_IDS["Васильев П.О."]}:Прошу всех внимательно ознакомиться с программой практики и требованиями к отчётам."
    "${STUDENT_IDS["Абрамов В.К."]}:Спасибо! Когда будут распределены руководители по проектам?"
    "${SUPERVISOR_IDS["Дмитриев А.С."]}:Распределение уже готово, проверьте свои чаты практик. Там вся информация."
    "${STUDENT_IDS["Тарасова Е.В."]}:А когда дедлайн по первому этапу?"
    "${SUPERVISOR_IDS["Ермакова О.В."]}:Первый этап — через 2 недели. Уточните у своих руководителей детали."
    "${SUPERVISOR_IDS["Жуков В.Н."]}:Коллеги, напоминаю про важность заполнения дневников практики."
    "${STUDENT_IDS["Черных Е.В."]}:Подскажите, где найти методические материалы?"
    "${SUPERVISOR_IDS["Кузнецова Е.В."]}:Все материалы в разделе 'Документы' вашей практики."
    "${SUPERVISOR_IDS["Новикова Л.П."]}:Обратите внимание: на следующей неделе будет вебинар по оформлению отчётов."
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

send_chat_messages() {
  local base_name="$1"
  shift
  local messages=("$@")

  CHAT_NAME="Чат практики: ${base_name}"
  CHAT_ID=$(echo "$ALL_CHATS" | jq -r --arg name "$CHAT_NAME" '.[]? | select(.name==$name) | .chat_id // empty')

  if [ -z "$CHAT_ID" ]; then
    echo "  Chat for $base_name not found, skipping..."
    return
  fi

  for msg in "${messages[@]}"; do
    IFS=':' read -r sender_id content <<< "$msg"
    curl -s -X POST "${API_URL}/messages" \
      -H "accept: application/json" \
      -H "Content-Type: application/json" \
      -d "{
        \"chat_id\": $CHAT_ID,
        \"sender_id\": $sender_id,
        \"content\": \"$content\"
      }" > /dev/null
  done
  echo "  ✓ Messages added to chat: $base_name (ID: $CHAT_ID)"
}

# ─── ПИ-101 ───
send_chat_messages "ТехноИнновации" \
  "${SUPERVISOR_IDS["Андреев К.И."]}:Добро пожаловать в ТехноИнновации! Начинаем работу над инновационными проектами." \
  "${STUDENT_IDS["Абрамов В.К."]}:Здравствуйте! Какой проект будет основным?" \
  "${SUPERVISOR_IDS["Андреев К.И."]}:Разработка IoT-платформы. Жду всех завтра на планировании." \
  "${STUDENT_IDS["Власов Д.Е."]}:Принято, подготовлю вопросы."

send_chat_messages "СофтЛаб" \
  "${SUPERVISOR_IDS["Белова М.А."]}:Коллеги, начинаем работу над проектом «Умный дом». Жду предложений." \
  "${STUDENT_IDS["Блинов А.П."]}:Отлично! Уже есть наработки по архитектуре."

send_chat_messages "КодМастер" \
  "${SUPERVISOR_IDS["Жуков В.Н."]}:Приветствую в КодМастер! Наша специализация — Python-разработка." \
  "${STUDENT_IDS["Гусев И.М."]}:Здравствуйте! Какой проект будем делать?" \
  "${SUPERVISOR_IDS["Жуков В.Н."]}:Система автоматизации тестирования. Подробности в понедельник."

send_chat_messages "ДевСтудио" \
  "${SUPERVISOR_IDS["Зайцева Т.А."]}:Веб-разработчики, добрый день! Наш проект — корпоративный портал." \
  "${STUDENT_IDS["Дроздов Н.С."]}:Принято! Используем React или Vue?" \
  "${SUPERVISOR_IDS["Зайцева Т.А."]}:React + TypeScript. Готовьте окружение."

# ─── ПИ-102 ───
send_chat_messages "ОблачныеРешения" \
  "${SUPERVISOR_IDS["Иванов С.Г."]}:ПИ-102, добро пожаловать в ОблачныеРешения. Тема: DevOps и облака." \
  "${STUDENT_IDS["Кабанов О.И."]}:Будем работать с AWS или Azure?" \
  "${SUPERVISOR_IDS["Иванов С.Г."]}:Основной упор на AWS. Всем зарегистрироваться в консоли." \
  "${STUDENT_IDS["Ларин В.П."]}:Уже сделано, жду инструкций."

send_chat_messages "МикросервисАрх" \
  "${SUPERVISOR_IDS["Кузнецова Е.В."]}:Тема нашей практики — микросервисы на Go. Изучите документацию." \
  "${STUDENT_IDS["Мартынов Е.С."]}:Есть! Начинаем с проектирования API?"

send_chat_messages "ТестировщикПлюс" \
  "${SUPERVISOR_IDS["Соколов М.А."]}:Добрый день! Будем осваивать Selenium и автотесты." \
  "${STUDENT_IDS["Николаева А.В."]}:Отлично, я уже установила окружение."

send_chat_messages "Киберфорум" \
  "${SUPERVISOR_IDS["Федоров А.В."]}:Безопасная разработка — наш профиль. Начинаем с OWASP Top 10." \
  "${STUDENT_IDS["Орлов Д.С."]}:Интересно! Жду материалы."

# ─── ПИ-103 ───
send_chat_messages "МобилАпп" \
  "${SUPERVISOR_IDS["Новикова Л.П."]}:МобилАпп приветствует ПИ-103! Разрабатываем мобильное приложение." \
  "${STUDENT_IDS["Павлов К.И."]}:Flutter или нативные?" \
  "${SUPERVISOR_IDS["Новикова Л.П."]}:Flutter, курс обучения скину в чат." \
  "${STUDENT_IDS["Романов А.В."]}:Спасибо, будем изучать!"

send_chat_messages "ГеймДевСтудия" \
  "${SUPERVISOR_IDS["Зайцева Т.А."]}:Геймдев команда, наш проект — 2D-платформер на Unity." \
  "${STUDENT_IDS["Сидоров М.Н."]}:Круто! Я возьму дизайн уровней." \
  "${STUDENT_IDS["Титова Е.В."]}:А я — программирование механик."

send_chat_messages "UXЛаб" \
  "${SUPERVISOR_IDS["Белова М.А."]}:UX/UI — это не просто дизайн, это наука. Начинаем с исследований." \
  "${STUDENT_IDS["Ушаков П.О."]}:Жду задания, готов проводить юзабилити-тестирование."

# ─── ИС-201 ───
send_chat_messages "ИнфоСистемы" \
  "${SUPERVISOR_IDS["Васильев П.О."]}:ИС-201, завтра в 10:00 встреча по проекту ERP-системы." \
  "${STUDENT_IDS["Тарасова Е.В."]}:Буду вовремя, подготовлю вопросы по архитектуре."

send_chat_messages "ERPЭксперт" \
  "${SUPERVISOR_IDS["Соколов М.А."]}:Внедряем 1С:ERP. Первый этап — обследование." \
  "${STUDENT_IDS["Устинова А.С."]}:Поняла, начинаю собирать требования."

send_chat_messages "БизнесАналитика" \
  "${SUPERVISOR_IDS["Новикова Л.П."]}:Бизнес-аналитика — мост между IT и бизнесом. Изучаем BABOK." \
  "${STUDENT_IDS["Филиппов Д.М."]}:Принято! Когда первая встреча с заказчиком?"

send_chat_messages "ДокументоОборот" \
  "${SUPERVISOR_IDS["Иванов С.Г."]}:Настраиваем СЭД. Основной инструмент — Directum." \
  "${STUDENT_IDS["Харитонова О.Н."]}:Поняла, жду доступы."

# ─── ИС-202 ───
send_chat_messages "ДатаЦентр" \
  "${SUPERVISOR_IDS["Ермакова О.В."]}:ИС-202, наша тема — проектирование ЦОД. Изучите материалы во вложении." \
  "${STUDENT_IDS["Черных Е.В."]}:Принято. Когда первая встреча?" \
  "${SUPERVISOR_IDS["Ермакова О.В."]}:В среду в 14:00." \
  "${STUDENT_IDS["Шаров Д.А."]}:Буду. Вопрос: будем использовать Cisco Packet Tracer?" \
  "${SUPERVISOR_IDS["Ермакова О.В."]}:Да, установите заранее."

send_chat_messages "СетевыеТехнологии" \
  "${SUPERVISOR_IDS["Федоров А.В."]}:Проектируем корпоративную сеть. Начинаем с топологии." \
  "${STUDENT_IDS["Щукина М.А."]}:Принято, жду схему."

send_chat_messages "ИТАутсорсинг" \
  "${SUPERVISOR_IDS["Кузнецова Е.В."]}:ИТ-аутсорсинг — поддержка клиентов. Готовьтесь к Service Desk." \
  "${STUDENT_IDS["Яковлев И.П."]}:Понял, жду инструктаж."

# ─── ИИ-301 ───
send_chat_messages "НейроТехнологии" \
  "${SUPERVISOR_IDS["Дмитриев А.С."]}:ИИ-301, начинаем исследования в области компьютерного зрения." \
  "${STUDENT_IDS["Андреева К.В."]}:Очень интересно! Какие датасеты будем использовать?" \
  "${SUPERVISOR_IDS["Дмитриев А.С."]}:COCO и ImageNet для начала."

send_chat_messages "МашинноеОбучение" \
  "${SUPERVISOR_IDS["Жуков В.Н."]}:Разработка ML-моделей для прогнозирования." \
  "${STUDENT_IDS["Борисов С.М."]}:Принято! Готовлю данные."

send_chat_messages "КомпьютерноеЗрение" \
  "${SUPERVISOR_IDS["Андреев К.И."]}:Распознавание образов — наше всё. OpenCV + Python." \
  "${STUDENT_IDS["Волкова Н.А."]}:Отлично, начинаю с фильтров."

send_chat_messages "РоботоТех" \
  "${SUPERVISOR_IDS["Соколов М.А."]}:Интеллектуальная робототехника. ROS + Gazebo." \
  "${STUDENT_IDS["Галкин Д.В."]}:Супер! Уже собираю симуляцию."

# ─── ИИ-302 ───
send_chat_messages "АналитикаДанных" \
  "${SUPERVISOR_IDS["Новикова Л.П."]}:ИИ-302, добро пожаловать! Тема: предиктивная аналитика." \
  "${STUDENT_IDS["Ершов И.В."]}:Здравствуйте! Будем использовать Python?" \
  "${SUPERVISOR_IDS["Новикова Л.П."]}:Да, Python + scikit-learn. Данные загружу завтра." \
  "${STUDENT_IDS["Жданова М.А."]}:Отлично, жду данные."

send_chat_messages "БигДата" \
  "${SUPERVISOR_IDS["Жуков В.Н."]}:Обработка больших данных. Spark + Hadoop." \
  "${STUDENT_IDS["Зуев К.С."]}:Принято! Настраиваю кластер."

send_chat_messages "НЛПЛаб" \
  "${SUPERVISOR_IDS["Дмитриев А.С."]}:NLP — обработка естественного языка. Трансформеры и BERT." \
  "${STUDENT_IDS["Исаева О.В."]}:Интересно! Уже читаю статьи."

# ─── КБ-401 ───
send_chat_messages "КиберЩит" \
  "${SUPERVISOR_IDS["Иванов С.Г."]}:КБ-401, наша задача — тестирование на проникновение. Настраиваем стенд." \
  "${STUDENT_IDS["Лобанов А.И."]}:Какие инструменты будем использовать?" \
  "${SUPERVISOR_IDS["Иванов С.Г."]}:Kali Linux, Metasploit, Wireshark. Всем установить." \
  "${STUDENT_IDS["Миронов Д.В."]}:Готово, жду задания."

send_chat_messages "ПентестЛаб" \
  "${SUPERVISOR_IDS["Федоров А.В."]}:Пентест — это искусство. Начинаем с разведки." \
  "${STUDENT_IDS["Назарова Е.П."]}:Принято! Запускаю сканирование."

send_chat_messages "Форензика" \
  "${SUPERVISOR_IDS["Ермакова О.В."]}:Цифровая криминалистика. Учимся собирать улики." \
  "${STUDENT_IDS["Овчинников Р.С."]}:Очень интересно! Жду образ диска."

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
echo -e ""
echo -e "${BLUE}Base distribution per group:${NC}"
echo -e "  ПИ-101: 4 базы (ТехноИнновации, СофтЛаб, КодМастер, ДевСтудио)"
echo -e "  ПИ-102: 4 базы (ОблачныеРешения, МикросервисАрх, ТестировщикПлюс, Киберфорум)"
echo -e "  ПИ-103: 3 базы (МобилАпп, ГеймДевСтудия, UXЛаб)"
echo -e "  ИС-201: 4 базы (ИнфоСистемы, ERPЭксперт, БизнесАналитика, ДокументоОборот)"
echo -e "  ИС-202: 3 базы (ДатаЦентр, СетевыеТехнологии, ИТАутсорсинг)"
echo -e "  ИИ-301: 4 базы (НейроТехнологии, МашинноеОбучение, КомпьютерноеЗрение, РоботоТех)"
echo -e "  ИИ-302: 3 базы (АналитикаДанных, БигДата, НЛПЛаб)"
echo -e "  КБ-401: 3 базы (КиберЩит, ПентестЛаб, Форензика)"
echo -e "${BLUE}========================================${NC}"
