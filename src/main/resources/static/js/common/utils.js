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

// 显示加载状态
function showLoading(element) {
    element.classList.add('loading');
}

// 隐藏加载状态
function hideLoading(element) {
    element.classList.remove('loading');
}

// 显示错误消息
function showError(message, element) {
    const errorDiv = document.createElement('div');
    errorDiv.className = 'error-message';
    errorDiv.textContent = message;
    element.appendChild(errorDiv);
    setTimeout(() => errorDiv.remove(), 3000);
}

// 显示成功消息
function showSuccess(message, element) {
    const successDiv = document.createElement('div');
    successDiv.className = 'success-message';
    successDiv.textContent = message;
    element.appendChild(successDiv);
    setTimeout(() => successDiv.remove(), 3000);
}

// 带Token的Fetch请求
async function fetchWithToken(url, options = {}) {
    const token = localStorage.getItem('accessToken');
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': token ? `Bearer ${token}` : ''
        }
    };

    try {
        const response = await fetch(contextPath + url, {
            ...defaultOptions,
            ...options,
            headers: {
                ...defaultOptions.headers,
                ...options.headers
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        return data;
    } catch (error) {
        console.error('Fetch error:', error);
        throw error;
    }
}

// 表单验证
function validateForm(form) {
    const inputs = form.querySelectorAll('input[required]');
    let isValid = true;

    inputs.forEach(input => {
        if (!input.value.trim()) {
            showError(`${input.name}不能为空`, input.parentElement);
            isValid = false;
        }
    });

    return isValid;
}

// 密码强度检查
function checkPasswordStrength(password) {
    const strongRegex = new RegExp("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\$%\^&\*])(?=.{8,})");
    const mediumRegex = new RegExp("^(((?=.*[a-z])(?=.*[A-Z]))|((?=.*[a-z])(?=.*[0-9]))|((?=.*[A-Z])(?=.*[0-9])))(?=.{6,})");

    if (strongRegex.test(password)) {
        return 'strong';
    } else if (mediumRegex.test(password)) {
        return 'medium';
    } else {
        return 'weak';
    }
}

// 邮箱验证
function validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

// 手机号验证
function validatePhone(phone) {
    const re = /^1[3-9]\d{9}$/;
    return re.test(phone);
}

// 防抖函数
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// 节流函数
function throttle(func, limit) {
    let inThrottle;
    return function executedFunction(...args) {
        if (!inThrottle) {
            func(...args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
}

// 格式化日期
function formatDate(date) {
    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

// 获取问候语
function getGreeting() {
    const hour = new Date().getHours();
    if (hour >= 5 && hour < 12) return '上午';
    if (hour >= 12 && hour < 18) return '下午';
    return '晚上';
}

// 导出工具函数
window.utils = {
    showLoading,
    hideLoading,
    showError,
    showSuccess,
    fetchWithToken,
    validateForm,
    checkPasswordStrength,
    validateEmail,
    validatePhone,
    debounce,
    throttle,
    formatDate,
    getGreeting
};
