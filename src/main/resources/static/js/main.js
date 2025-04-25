// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    // 表单验证示例
    const forms = document.querySelectorAll('.needs-validation');
    Array.from(forms).forEach(form => {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        }, false);
    });

    // 显示错误信息
    const errorElements = document.querySelectorAll('.text-danger');
    errorElements.forEach(element => {
        if (element.textContent) {
            // 可以添加动画效果或其他处理
        }
    });
});