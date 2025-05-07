import api from './api';
import { adaptUserFromAPI, adaptUserToAPI } from '../utils/adapters';

export const getAllUsers = async () => {
  try {
    const userData = JSON.parse(localStorage.getItem('userData'));
    const userRole = userData.role;
    
    const response = await api.get(`/pet-and-health/${userRole === 'admin' ? 'admin' : 'vet'}/users`);
    
    if (!response.data) return [];

    return Array.isArray(response.data) 
      ? response.data.map(user => adaptUserFromAPI(user))
      : [];
  } catch (error) {
    console.error('Error fetching users:', error);
    throw error;
  }
};

export const getUserById = async (userId) => {
  try {
    const response = await api.get(`/pet-and-health/admin/users/${userId}`);
    return adaptUserFromAPI(response.data);
  } catch (error) {
    console.error('Error fetching user info:', error);
    throw error;
  }
};

export const createUser = async (userData) => {
  try {
    const response = await api.post('/pet-and-health/admin/users', userData);
    return response.data;
  } catch (error) {
    console.error('Error creating user:', error);
    throw error;
  }
};

export const updateUserAdmin = async (userData) => {
  try {
    const response = await api.put(`/pet-and-health/admin/users/${userData._id}`, userData);
    return response.data;
  } catch (error) {
    console.error('Error updating user:', error);
    throw error;
  }
};
  
export const deleteUser = async (userId) => {
  try {
    const response = await api.delete(`/pet-and-health/admin/users/${userId}`);
    return response.data;
  } catch (error) {
    throw error;
  }
};
  
export const getUserInfo = async () => {
  try {
    const userData = JSON.parse(localStorage.getItem('userData'));
    const userRole = userData.role;
    
    const url = `/pet-and-health/${userRole === 'admin' ? 'admin' : 'vet'}/profile`;
    
    const response = await api.get(url);
    return response.data;
  } catch (error) {
    console.error('Error fetching user profile:', error);
    throw error;
  }
};

export const updateUserProfile = async (userDataToUpdate) => {
  try {
    const storedUserData = JSON.parse(localStorage.getItem('userData'));
    const userRole = storedUserData.role;
    
    const url = `/pet-and-health/${userRole === 'admin' ? 'admin' : 'vet'}/profile`;
    
    const response = await api.put(url, userDataToUpdate);
    return response.data;
  } catch (error) {
    console.error('Error updating user profile:', error);
    throw error;
  }
};

export const updateUser = updateUserProfile;