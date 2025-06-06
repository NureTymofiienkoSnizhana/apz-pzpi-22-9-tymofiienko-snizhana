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
