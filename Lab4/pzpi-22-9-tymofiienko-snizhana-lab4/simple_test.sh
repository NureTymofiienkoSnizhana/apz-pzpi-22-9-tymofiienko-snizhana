
echo "🐳 Простий тест Docker v2..."

# Перевірка Docker
if ! docker ps &> /dev/null; then
    echo "❌ Немає прав для Docker"
    exit 1
fi

echo "✅ Docker працює"

# Очищення старих контейнерів
echo "🧹 Очищення..."
docker stop pet-backend-1 pet-mongodb 2>/dev/null || true
docker rm pet-backend-1 pet-mongodb 2>/dev/null || true

# Створення мережі
echo "🌐 Створення мережі..."
docker network create pet-health-net 2>/dev/null || true

# Запуск MongoDB
echo "🗄️ Запуск MongoDB..."
docker run -d \
    --name pet-mongodb \
    --network pet-health-net \
    -e MONGO_INITDB_ROOT_USERNAME=welnersis \
    -e MONGO_INITDB_ROOT_PASSWORD='password' \
    -e MONGO_INITDB_DATABASE=PetAndHealth \
    mongo:6.0

sleep 15

# Запуск бекенду з повними змінними оточення
echo "🚀 Запуск бекенду..."
docker run -d \
    --name pet-backend-1 \
    --network pet-health-net \
    -p 8080:8080 \
    -e PORT=8080 \
    -e MONGO_USERNAME=welnersis \
    -e MONGO_PASSWORD='password' \
    -e JWT_SECRET='token' \
    -e SMTP_HOST=smtp.gmail.com \
    -e SMTP_PORT=587 \
    -e SMTP_USERNAME=timofeenko1404@gmail.com \
    -e SMTP_PASSWORD='password' \
    -e SMTP_FROM_NAME='Pet Health System' \
    pet-health-backend:latest

sleep 20

# Перевірка доступності
echo "🔍 Перевірка доступності..."
for i in {1..15}; do
    if curl -s http://localhost:8080 > /dev/null 2>&1; then
        echo "✅ Сервіс доступний на http://localhost:8080"
        echo "🌐 Тестуємо API endpoint..."
        curl -s http://localhost:8080/api/pet-and-health/ || echo "API endpoint недоступний"
        break
    else
        echo "⏳ Очікуємо запуску... ($i/15)"
        sleep 5
    fi

    if [ $i -eq 15 ]; then
        echo "❌ Сервіс недоступний. Логи:"
        docker logs pet-backend-1
        echo ""
        echo "MongoDB логи:"
        docker logs pet-mongodb
    fi
done

# Показати статус
echo "📋 Статус контейнерів:"
docker ps

echo ""
echo "🧹 Для очищення виконайте:"
echo "docker stop pet-backend-1 pet-mongodb"
echo "docker rm pet-backend-1 pet-mongodb"
echo "docker network rm pet-health-net"

