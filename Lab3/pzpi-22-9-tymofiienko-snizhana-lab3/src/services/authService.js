import api from './api';

export const login = async (email, password) => {
  try {
    const response = await api.post('/pet-and-health/login/auth', {
      email,
      password
    });
    
    if (!response.data) {
      throw new Error('Сервер повернув порожню відповідь');
    }
    
    const { message, user_id, role, token } = response.data;
    
    if (!token) {
      throw new Error('Токен не отримано від сервера');
    }
    
    if (!user_id || !role) {
      throw new Error('Дані користувача не отримано від сервера');
    }
    
    localStorage.setItem('token', token);
 
    const userData = {
      id: user_id,
      role: role,
      email: email 
    };
    
    localStorage.setItem('userData', JSON.stringify(userData));
    
    return userData;
  } catch (error) {
    
    if (error.response) {
      throw new Error(
        error.response.data?.error || 
        `Помилка при вході: ${error.response.status}. Перевірте свої дані і спробуйте знову.`
      );
    } else if (error.request) {
      throw new Error('Сервер не відповідає. Перевірте своє підключення до Інтернету.');
    } else {
      throw new Error(`Помилка при вході: ${error.message}`);
    }
  }
};

export const forgotPassword = async (email) => {
  try {
      const response = await api.put('/pet-and-health/login/forgot-password', { email });

      if (!response.data) {
          throw new Error('Не вдалося відновити пароль');
      }
    
      return response.data;
  } catch (error) {
      throw error;
  }
};

export const logout = async () => {
  try {
    const token = localStorage.getItem('token');
    
    if (token) {
      await api.post('/pet-and-health/login/logout', {}, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
    }
    
    localStorage.removeItem('token');
    localStorage.removeItem('userData'); 
    
    return true;
  } catch (error) {
    console.error('Помилка при виході:', error);
    localStorage.removeItem('token');
    localStorage.removeItem('userData');
    throw error;
  }
};

export const getUserRole = () => {
  const userData = localStorage.getItem('userData');
  if (userData) {
      const parsed = JSON.parse(userData);
      return parsed.role;
  }
  return null;
};