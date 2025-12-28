const API_BASE_URL = '/api';
const ENDPOINTS = {
    USERS: `${API_BASE_URL}/user/findAll`,
    BINARY_CONTENT: `${API_BASE_URL}/binaryContent/find`
};

// 기본 이미지 경로 설정
const DEFAULT_AVATAR_PATH = '/default-avatar.png';

document.addEventListener('DOMContentLoaded', () => {
    fetchAndRenderUsers();
});

async function fetchAndRenderUsers() {
    try {
        const response = await fetch(ENDPOINTS.USERS);
        if (!response.ok) throw new Error('데이터를 불러오는데 실패했습니다.');
        const users = await response.json();
        renderUserList(users);
    } catch (error) {
        console.error('Fetch Error:', error);
    }
}

// 프로필 이미지를 가져오는 함수
async function fetchUserProfile(profileImageId) {
    try {
        const response = await fetch(`${ENDPOINTS.BINARY_CONTENT}?binaryContentId=${profileImageId}`);
        if (!response.ok) throw new Error('이미지 로딩 실패');

        const profile = await response.json();
        // Base64 데이터를 Data URL 형식으로 변환하여 반환
        return `data:${profile.contentType};base64,${profile.base64Data}`;
    } catch (error) {
        console.error('Profile image fetch error:', error);
        // API 호출 실패 시 로컬 기본 이미지 반환
        return DEFAULT_AVATAR_PATH;
    }
}

async function renderUserList(users) {
    const userListElement = document.getElementById('userList');
    userListElement.innerHTML = '';

    for (const user of users) {
        // 1. profileImageId가 있으면 API 호출, 없으면 기본 이미지 경로 할당
        const profileUrl = user.profileImageId ?
            await fetchUserProfile(user.profileImageId) :
            DEFAULT_AVATAR_PATH;

        const card = document.createElement('div');
        card.className = 'user-card';
        card.innerHTML = `
            <div class="avatar-wrapper">
                <img src="${profileUrl}" alt="${user.username}" class="user-avatar">
                <span class="status-dot ${user.online ? 'dot-online' : 'dot-offline'}"></span>
            </div>
            <div class="user-name">${user.username}</div>
            <div class="user-email">${user.email}</div>
        `;
        userListElement.appendChild(card);
    }
}