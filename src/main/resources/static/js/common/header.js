// header 依据登录状态动态渲染
document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem('accessToken');
    const navbarItems = document.getElementById('navbarItems');
    const headerHomepage = document.getElementById('headerHomepage');
    const headerEditInfo = document.getElementById('headerEditInfo');
    const headerAdminPage = document.getElementById('headerAdminPage');
    const headerLogin = document.getElementById('headerLogin');
    const headerRegister = document.getElementById('headerRegister');
    const headerLogout = document.getElementById('headerLogout');

    // 检查用户登录状态
    if (token) {
        // 用户已登录，显示个人主页、编辑信息和登出按钮
        headerHomepage.style.display = 'block';
        headerEditInfo.style.display = 'block';
        headerLogout.style.display = 'block';
        headerLogin.style.display = 'none';
        headerRegister.style.display = 'none';

        // 检查用户角色
        checkUserRole();
    } else {
        // 用户未登录，显示登录和注册按钮
        headerHomepage.style.display = 'none';
        headerEditInfo.style.display = 'none';
        headerAdminPage.style.display = 'none';
        headerLogout.style.display = 'none';
        headerLogin.style.display = 'block';
        headerRegister.style.display = 'block';
    }

    // 登出按钮点击事件
    if (headerLogout) {
        headerLogout.addEventListener('click', function(e) {
            e.preventDefault();
            logout();
        });
    }

    // 检查用户角色
    async function checkUserRole() {
        try {
            const response = await utils.fetchWithToken('user/info');
            if (response.code === 0 && response.data) {
                const userRole = response.data.role;
                if (userRole === 'ADMIN') {
                    headerAdminPage.style.display = 'block';
                }
            }
        } catch (error) {
            console.error('获取用户角色失败:', error);
        }
    }

    // 登出函数
    async function logout() {
        try {
            const response = await utils.fetchWithToken('user/logout', {
                method: 'POST'
            });

            if (response.code === 0) {
                localStorage.removeItem('accessToken');
                window.location.href = contextStatic + 'index';
            } else {
                throw new Error(response.message || '登出失败');
            }
        } catch (error) {
            console.error('登出失败:', error);
            utils.showError('登出失败: ' + error.message, navbarItems);
        }
    }
});