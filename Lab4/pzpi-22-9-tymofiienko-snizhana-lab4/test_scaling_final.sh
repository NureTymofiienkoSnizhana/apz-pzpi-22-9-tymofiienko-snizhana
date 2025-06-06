#!/bin/bash

echo "🐳 Тестування масштабування Pet Health системи..."

# Перевірка Locust
if ! command -v locust &> /dev/null; then
    echo "📦 Встановлення Locust..."
    pip3 install locust
fi

# Створення locustfile.py
echo "📝 Створення locustfile.py..."
cat > locustfile.py << 'LOCUST_EOF'
from locust import HttpUser, task, between
import json
import random

class PetHealthUser(HttpUser):
    wait_time = between(1, 3)

    @task(5)
    def test_root(self):
        """Тестуємо кореневий endpoint"""
        self.client.get("/", catch_response=True)

    @task(3)
    def test_api_root(self):
        """Тестуємо API кореневий endpoint"""
        with self.client.get("/api/pet-and-health/", catch_response=True) as response:
            if "Authorization header required" in response.text:
                response.success()

    @task(2)
    def test_health_check(self):
        """Тестуємо health check"""
        self.client.get("/health", catch_response=True)

    @task(1)
    def test_login_page(self):
        """Тестуємо login endpoint"""
        with self.client.get("/api/pet-and-health/login/", catch_response=True) as response:
            response.success()
LOCUST_EOF

# Функція тестування
test_with_instances() {
    local instances=$1
    echo "=== ТЕСТ: $instances ІНСТАНС(ІВ) ==="

    # Очищення
    echo "🧹 Очищення попередніх контейнерів..."
    docker ps -q --filter "name=pet-" | while read -r container_id; do
        if [ -n "$container_id" ]; then
            docker stop "$container_id" 2>/dev/null || true
        fi
    done

    docker ps -aq --filter "name=pet-" | while read -r container_id; do
        if [ -n "$container_id" ]; then
            docker rm "$container_id" 2>/dev/null || true
        fi
    done

    # Створення мережі
    docker network create pet-health-net 2>/dev/null || true

    # Запуск MongoDB
    echo "🗄️ Запуск MongoDB..."
    docker run -d \
        --name pet-mongodb \
        --network pet-health-net \
        -e MONGO_INITDB_ROOT_USERNAME=welnersis \
        -e MONGO_INITDB_ROOT_PASSWORD=testpass123 \
        mongo:6.0

    echo "⏳ Очікування запуску MongoDB..."
    sleep 15

    # Запуск бекенду
    echo "🚀 Запуск $instances інстанс(ів) бекенду..."
    for i in $(seq 1 $instances); do
        local port=$((8080 + i - 1))
        docker run -d \
            --name pet-backend-$i \
            --network pet-health-net \
            -p $port:8080 \
            -e PORT=8080 \
            -e MONGO_USERNAME=welnersis \
            -e MONGO_PASSWORD=testpass123 \
            -e JWT_SECRET=test-secret \
            pet-health-backend:latest
        echo "✅ Запущено pet-backend-$i на порті $port"
    done

    echo "⏳ Очікування запуску бекенду..."
    sleep 25

    # Перевірка доступності
    echo "🔍 Перевірка доступності на порті 8080..."
    service_available=false
    for i in {1..10}; do
        if curl -s http://localhost:8080 > /dev/null 2>&1; then
            echo "✅ Сервіс доступний"
            service_available=true
            break
        else
            echo "⏳ Очікуємо... ($i/10)"
            sleep 3
        fi
    done

    if [ "$service_available" = false ]; then
        echo "❌ Сервіс недоступний після очікування"
        return 1
    fi

    # Тест з Locust
    mkdir -p test_results
    echo "🚀 Запуск Locust тесту для $instances інстанса(ів)..."

    if locust -f locustfile.py \
           --host=http://localhost:8080 \
           --users=500 \
           --spawn-rate=20 \
           --run-time=120s \
           --html=test_results/report_${instances}_instances.html \
           --csv=test_results/results_${instances}_instances \
           --headless; then
        echo "✅ Locust тест завершено успішно"
    else
        echo "❌ Помилка під час виконання Locust тесту"
        return 1
    fi

    echo "📊 Результат для $instances інстанс(ів) збережено"

    # Показати статистику
    echo "📈 Статистика контейнерів:"
    if docker ps --filter "name=pet-" --format "table {{.Names}}\t{{.Status}}" | grep -q pet-; then
        docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}" $(docker ps -q --filter "name=pet-")
    else
        echo "Немає активних контейнерів"
    fi

    sleep 5
}

# Функція очищення
cleanup() {
    echo "🧹 Очищення ресурсів..."
    docker ps -q --filter "name=pet-" | while read -r container_id; do
        if [ -n "$container_id" ]; then
            docker stop "$container_id" 2>/dev/null || true
        fi
    done

    docker ps -aq --filter "name=pet-" | while read -r container_id; do
        if [ -n "$container_id" ]; then
            docker rm "$container_id" 2>/dev/null || true
        fi
    done

    docker network rm pet-health-net 2>/dev/null || true
}

# Обробка сигналів для коректного завершення
trap cleanup EXIT INT TERM

# Основний процес
echo "🔨 Перевірка образу..."
if ! docker images | grep -q pet-health-backend; then
    echo "🔨 Збірка образу..."
    if ! docker build -t pet-health-backend:latest .; then
        echo "❌ Помилка збірки образу"
        exit 1
    fi
fi

# Тести
echo "🧪 Початок тестування..."
for instances in 1 2 3; do
    if ! test_with_instances $instances; then
        echo "❌ Тестування з $instances інстанс(ами) провалилося"
        # Продовжуємо з наступним тестом замість завершення
    fi
    echo ""
done

echo "🎉 Всі тести завершено!"
echo ""
echo "📊 Результати тестування:"
if [ -d "test_results" ] && [ "$(ls -A test_results 2>/dev/null)" ]; then
    ls -la test_results/
else
    echo "Папка з результатами порожня або відсутня"
fi

echo ""
echo "🔗 Відкрийте HTML звіти в браузері:"
if ls test_results/report_*.html 1> /dev/null 2>&1; then
    for file in test_results/report_*.html; do
        if [ -f "$file" ]; then
            echo "   file://$PWD/$file"
        fi
    done
else
    echo "HTML звіти не знайдено"
fi

echo "✨ Тестування завершено!"