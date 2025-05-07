import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { logout } from '../../services/authService';
import { useAuth } from '../../context/AuthContext';

const Logout = () => {
  const navigate = useNavigate();
  const { setCurrentUser } = useAuth();
  
  useEffect(() => {
    const performLogout = async () => {
      try {
        await logout();

        setCurrentUser(null);
        
        navigate('/login');
      } catch (error) {
        console.error('Помилка при виході:', error);
        setCurrentUser(null);
        navigate('/login');
      }
    };
    
    performLogout();
  }, [navigate, setCurrentUser]);
  
  return <div className="logout-page">Виходимо з системи...</div>;
};

export default Logout;