window.onload = function () {
    const contextPath = "/authcheckdemo/api";  // ✅ 固定前缀
    const userInfoUrl = `${contextPath}/user/info`;

    fetchWithToken(userInfoUrl)
        .then(response => {
            if (!response.ok) throw new Error("获取用户信息失败");
            return response.json();
        })
        .then(data => {
            const greeting = getGreeting();
            document.getElementById("greetingText").textContent = `【${greeting}】你好，${data.username}`;
            document.getElementById("userInfo").innerHTML = `
                <p>账号：${data.username}</p>
                <p>登录时间：${data.loginTime}</p>
            `;
        })
        .catch(err => {
            console.error("加载失败", err);
            alert("请先登录！");
            window.location.href = "/index.html";
        });
};

function getGreeting() {
    const hour = new Date().getHours();
    if (hour < 12) return "早上好";
    if (hour < 18) return "下午好";
    return "晚上好";
}