import axios from 'axios';

// Configuration de l'URL de base de l'API
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

// Créer une instance axios configurée
export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10 secondes
});

// Intercepteur pour les requêtes (ajout de tokens, etc.)
apiClient.interceptors.request.use(
  (config) => {
    // Ajouter un token d'authentification si nécessaire
    // const token = localStorage.getItem('token');
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`;
    // }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Intercepteur pour les réponses (gestion d'erreurs globale)
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // Gestion d'erreurs globale
    if (error.response) {
      // Erreur de réponse du serveur
      console.error('Erreur API:', error.response.status, error.response.data);
    } else if (error.request) {
      // Pas de réponse du serveur
      console.error('Pas de réponse du serveur');
    } else {
      // Erreur de configuration
      console.error('Erreur de configuration:', error.message);
    }
    return Promise.reject(error);
  }
);

export default apiClient;