// header 依据登录状态动态渲染
document.addEventListener("DOMContentLoaded", function () {
    const isLoggedIn = localStorage.getItem("accessToken") != null;
    // 如果有 accessToken，表示已登录
    document.getElementById('headerLogin').style.display = isLoggedIn ? 'none' : 'inline-block';
    document.getElementById('headerRegister').style.display = isLoggedIn ? 'none' : 'inline-block';
    document.getElementById('headerHomepage').style.display = isLoggedIn ? 'inline-block' : 'none';
    document.getElementById('headerEditInfo').style.display = isLoggedIn ? 'inline-block' : 'none';
    document.getElementById('headerLogout').style.display = isLoggedIn ? 'inline-block' : 'none';

    // 判断是否为管理员
    const userRole = localStorage.getItem("userRole"); // 获取用户角色
    if (userRole === 'ADMIN') {
        document.getElementById('adminPage').style.display = 'inline-block';
    }

    // 如果点击“个人主页”而未登录，跳转到登录页
    document.getElementById('personalHomepage')?.addEventListener('click', function (event) {
        if (!accessToken) {
            event.preventDefault(); // 阻止默认行为
            window.location.href = '/auth/login'; // 跳转到登录页面
        }
    });
});

// 登出操作,清理本地状态
function handleLogout() {
    // 清除本地 token 和角色信息
    localStorage.removeItem('accessToken');
    localStorage.removeItem('userRole');
    // 可选：跳转到登录页面
    window.location.href = contextStatic;
}

// 给 "登出" 按钮绑定点击事件
document.getElementById('headerLogout')?.addEventListener('click', async function (event) {
    event.preventDefault(); // 阻止默认跳转

    try {
        await fetchWithToken('user/logout', {
            method: 'POST',
        });
    } catch (error) {
        console.error('登出请求失败:', error);
    } finally {
        // 无论请求成功或失败，都清除本地状态
        handleLogout();
    }
});