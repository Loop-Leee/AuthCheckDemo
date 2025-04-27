const contextPath = "/authcheckdemo/api/"
const contextStatic = "/authcheckdemo/"

// 自动加载 Header 和 Footer
document.addEventListener('DOMContentLoaded', function () {

    // 加载 Header
    fetch(contextStatic + '/pages/fragments/header.html')
        .then(response => response.text())
        .then(data => {
            const header = document.getElementById('header-placeholder');
            if (header) {
                header.innerHTML = data;  // 如果页面中声明了 header-placeholder 才加载
            }
        });

    // 加载 Footer
    fetch(contextStatic + '/pages/fragments/footer.html')
        .then(response => response.text())
        .then(data => {
            const footer = document.getElementById('footer-placeholder');
            if (footer) {
                footer.innerHTML = data;  // 如果页面中声明了 foot-placeholder 才加载
            }
        });
});

// 封装的 fetch 方法, 传入后端接口路径, 自动添加Authorization请求头发送请求, 获取 json 返回结果
function fetchWithToken(url, options = {}) {
    //  调试日志:记录函数调用和请求 URL
    console.log("fetchWithToken called with URL:", url);
    // 从本地存储获取accessToken
    const token = localStorage.getItem("accessToken");
    // Token检查：如果不存在Token则跳转到登录页面
    if (!token) {
        alert("请重新登录！");
        window.location.href = contextStatic + "index";
        return Promise.reject("Token missing");
    }

    return fetch(contextPath + url, {
        ...options,
        headers: {
            ...options.headers,       // 保留调用方自定义的 headers
            Authorization: `Bearer ${token}`, // 添加 Bearer Token 认证
            "Content-Type": "application/json" // 固定内容类型为 JSON
        },
    })
        .then(response => {
            // 2xx状态码: 解析并返回 JSON 数据
            if (response.ok) {
                return response.json();
            } else {
                throw new Error(`请求失败: ${response.status}`);
            }
        })
        .catch(error => {
            // 统一错误处理：提示用户并跳转登录页
            alert("获取用户信息失败：" + error.message);
            console.error('获取用户信息请求失败：', error.message);
            // 跳转到登录页面（注意：contextPath需要根据实际项目配置）
            window.location.href = contextPath + "/auth/login";
            // 继续传递错误以便链式调用可以处理
            return Promise.reject(error);
        })
}
