import api from './api';
import { adaptPetFromAPI, adaptPetToAPI } from '../utils/adapters';

export const getAllPets = async () => {
  try {
    const userData = JSON.parse(localStorage.getItem('userData'));
    const userRole = userData.role;
    
    const url = `/pet-and-health/${userRole === 'admin' ? 'admin' : 'vet'}/pets`;
    
    const response = await api.get(url);
    
    if (!response.data) return [];

    return Array.isArray(response.data) 
      ? response.data.map(pet => adaptPetFromAPI(pet))
      : [];
  } catch (error) {
      console.error('Error fetching pets:', error);
      throw error;
  }
};

export const getPetInfo = async (petId) => {
  try {
    const userData = JSON.parse(localStorage.getItem('userData'));
    const userRole = userData.role;
    
    const url = `/pet-and-health/${userRole === 'admin' ? 'admin' : 'vet'}/pets/${petId}`;
    
    const response = await api.get(url);
    return adaptPetFromAPI(response.data);
  } catch (error) {
    console.error('Error fetching pet info:', error);
    throw error;
  }
};

export const addPet = async (petData) => {
  try {
    const response = await api.post('/pet-and-health/admin/pets', petData);
    return response.data;
  } catch (error) {
    console.error('Error adding pet:', error);
    throw error;
  }
};

export const updatePet = async (petData) => {
  try {
    const petId = petData.id || petData._id;
    const response = await api.put(`/pet-and-health/admin/pets/${petId}`, petData);
    return response.data;
  } catch (error) {
    console.error('Error updating pet:', error);
    throw error;
  }
};

export const deletePet = async (petId) => {
  try {
    const response = await api.delete(`/pet-and-health/admin/pets/${petId}`);
    return response.data;
  } catch (error) {
    console.error('Error deleting pet:', error);
    throw error;
  }
};

export const getPetReport = async (petId, startTime, endTime) => {
  try {
    // Отримуємо дані з localStorage для визначення ролі
    const userData = JSON.parse(localStorage.getItem('userData'));
    const userRole = userData.role;
    
    // Оновлюємо URL в залежності від ролі користувача
    let url = '/pet-and-health';
    
    if (userRole === 'admin' || userRole === 'vet') {
      url += `/${userRole}/pets/${petId}/report`;
    } else {
      throw new Error('Недостатньо прав для генерації звіту');
    }
    
    const response = await api.post(url, {
      start_time: startTime,
      end_time: endTime
    }, {
      responseType: 'blob'
    });

    const startDate = new Date(startTime * 1000);
    const endDate = new Date(endTime * 1000);
    
    const startDateStr = startDate.toLocaleDateString('uk-UA');
    const endDateStr = endDate.toLocaleDateString('uk-UA');
    
    const fileName = `Pet_Report_${startDateStr}_to_${endDateStr}.pdf`;

    const url_blob = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url_blob;
    link.setAttribute('download', fileName);
    document.body.appendChild(link);
    link.click();
    
    window.URL.revokeObjectURL(url_blob);
    link.remove();
    
    return true;
  } catch (error) {
    console.error('Error generating pet report:', error);
    throw error;
  }
};