import api from './api';

export const getAllDevices = async () => {
  try {
    const response = await api.get('/pet-and-health/admin/devices');
    
    if (!response.data) return [];

    return Array.isArray(response.data) 
      ? response.data.map(device => ({
          id: device.ID || device._id,
          serial_number: device.SerialNumber || device.serial_number,
          model: device.Model || device.model,
          status: device.Status || device.status,
          pet_id: device.PetID || device.pet_id,
          pet_name: device.PetName || device.pet_name,
          pet_owner: device.PetOwner || device.pet_owner
        }))
      : [];
  } catch (error) {
    console.error('Error fetching devices:', error);
    throw error;
  }
};

export const addDevice = async (deviceData) => {
  try {
    const response = await api.post('/pet-and-health/admin/devices', deviceData);
    return response.data;
  } catch (error) {
    console.error('Error adding device:', error);
    throw error;
  }
};

export const updateDevice = async (deviceData) => {
  try {
    const deviceId = deviceData.id || deviceData._id || deviceData.ID;
    const response = await api.put(`/pet-and-health/admin/devices/${deviceId}`, deviceData);
    return response.data;
  } catch (error) {
    console.error('Error updating device:', error);
    throw error;
  }
};

export const deleteDevice = async (deviceId) => {
  try {
    const response = await api.delete(`/pet-and-health/admin/devices/${deviceId}`);
    return response.data;
  } catch (error) {
    console.error('Error deleting device:', error);
    throw error;
  }
};

export const assignDevice = async (deviceId, petId) => {
  try {
    const response = await api.post(`/pet-and-health/admin/devices/${deviceId}/assign`, {
      pet_id: petId
    });
    return response.data;
  } catch (error) {
    console.error('Error assigning device:', error);
    throw error;
  }
};

export const unassignDevice = async (deviceId) => {
  try {
    const response = await api.post(`/pet-and-health/admin/devices/${deviceId}/unassign`, {});
    return response.data;
  } catch (error) {
    console.error('Error unassigning device:', error);
    throw error;
  }
};