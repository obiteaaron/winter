// 平滑滚动
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            const headerOffset = 80;
            const elementPosition = target.getBoundingClientRect().top;
            const offsetPosition = elementPosition + window.pageYOffset - headerOffset;

            window.scrollTo({
                top: offsetPosition,
                behavior: 'smooth'
            });
        }
    });
});

// 导航栏滚动效果
let lastScroll = 0;
const navbar = document.querySelector('.navbar');

window.addEventListener('scroll', () => {
    const currentScroll = window.pageYOffset;

    if (currentScroll > 100) {
        navbar.style.boxShadow = '0 2px 12px rgba(0, 0, 0, 0.15)';
    } else {
        navbar.style.boxShadow = '0 2px 8px rgba(0, 0, 0, 0.1)';
    }

    lastScroll = currentScroll;
});

// 元素进入视口动画
const observerOptions = {
    root: null,
    rootMargin: '0px',
    threshold: 0.1
};

const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.style.opacity = '1';
            entry.target.style.transform = 'translateY(0)';
        }
    });
}, observerOptions);

// 观察所有特性卡片
document.querySelectorAll('.feature-card').forEach((card, index) => {
    card.style.opacity = '0';
    card.style.transform = 'translateY(30px)';
    card.style.transition = `all 0.6s ease ${index * 0.1}s`;
    observer.observe(card);
});

// 观察所有模块项
document.querySelectorAll('.module-item').forEach((item, index) => {
    item.style.opacity = '0';
    item.style.transform = 'translateY(30px)';
    item.style.transition = `all 0.6s ease ${index * 0.1}s`;
    observer.observe(item);
});

// 观察步骤
document.querySelectorAll('.step').forEach((step, index) => {
    step.style.opacity = '0';
    step.style.transform = 'translateY(30px)';
    step.style.transition = `all 0.6s ease ${index * 0.2}s`;
    observer.observe(step);
});

// 数字动画
function animateValue(element, start, end, duration) {
    const range = end - start;
    const increment = end > start ? 1 : -1;
    const stepTime = Math.abs(Math.floor(duration / range));
    let current = start;

    const timer = setInterval(() => {
        current += increment;
        element.textContent = current + '+';
        if (current === end) {
            clearInterval(timer);
        }
    }, stepTime);
}

// 统计数字动画
const statsObserver = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            const statNumbers = entry.target.querySelectorAll('.stat-number');
            statNumbers.forEach(stat => {
                const text = stat.textContent;
                if (text.includes('+')) {
                    const num = parseInt(text);
                    animateValue(stat, 0, num, 2000);
                }
            });
            statsObserver.unobserve(entry.target);
        }
    });
}, { threshold: 0.5 });

const heroStats = document.querySelector('.hero-stats');
if (heroStats) {
    statsObserver.observe(heroStats);
}

// 代码块复制功能
document.querySelectorAll('.code-block').forEach(block => {
    const pre = block.querySelector('pre');
    if (pre) {
        // 创建复制按钮
        const copyBtn = document.createElement('button');
        copyBtn.className = 'copy-btn';
        copyBtn.innerHTML = '复制';
        copyBtn.style.cssText = `
            position: absolute;
            top: 10px;
            right: 10px;
            background: rgba(255, 255, 255, 0.1);
            color: white;
            border: 1px solid rgba(255, 255, 255, 0.3);
            padding: 5px 15px;
            border-radius: 4px;
            cursor: pointer;
            font-size: 12px;
            transition: all 0.3s;
        `;

        block.style.position = 'relative';
        block.appendChild(copyBtn);

        copyBtn.addEventListener('click', async () => {
            const code = pre.textContent;
            try {
                await navigator.clipboard.writeText(code);
                copyBtn.innerHTML = '已复制';
                copyBtn.style.background = 'rgba(82, 196, 26, 0.3)';
                copyBtn.style.borderColor = 'rgba(82, 196, 26, 0.5)';

                setTimeout(() => {
                    copyBtn.innerHTML = '复制';
                    copyBtn.style.background = 'rgba(255, 255, 255, 0.1)';
                    copyBtn.style.borderColor = 'rgba(255, 255, 255, 0.3)';
                }, 2000);
            } catch (err) {
                console.error('复制失败:', err);
            }
        });

        copyBtn.addEventListener('mouseenter', () => {
            copyBtn.style.background = 'rgba(255, 255, 255, 0.2)';
        });

        copyBtn.addEventListener('mouseleave', () => {
            copyBtn.style.background = 'rgba(255, 255, 255, 0.1)';
        });
    }
});

// 响应式导航菜单
const navMenu = document.querySelector('.nav-menu');
let menuOpen = false;

// 创建移动端菜单按钮
const menuBtn = document.createElement('button');
menuBtn.className = 'mobile-menu-btn';
menuBtn.innerHTML = '☰';
menuBtn.style.cssText = `
    display: none;
    background: none;
    border: none;
    font-size: 24px;
    color: var(--text-color);
    cursor: pointer;
    padding: 5px;
`;

// 添加移动端样式
const style = document.createElement('style');
style.textContent = `
    @media (max-width: 768px) {
        .mobile-menu-btn {
            display: block !important;
        }

        .nav-menu {
            display: none;
            position: absolute;
            top: 100%;
            left: 0;
            right: 0;
            background: white;
            flex-direction: column;
            padding: 20px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            gap: 15px;
        }

        .nav-menu.active {
            display: flex;
        }

        .nav-menu a {
            padding: 10px;
            border-bottom: 1px solid var(--border-color);
        }

        .nav-menu a:last-child {
            border-bottom: none;
        }
    }
`;
document.head.appendChild(style);

document.querySelector('.navbar .container').appendChild(menuBtn);

menuBtn.addEventListener('click', () => {
    menuOpen = !menuOpen;
    navMenu.classList.toggle('active');
    menuBtn.innerHTML = menuOpen ? '✕' : '☰';
});

// 点击页面其他地方关闭菜单
document.addEventListener('click', (e) => {
    if (menuOpen && !e.target.closest('.navbar')) {
        menuOpen = false;
        navMenu.classList.remove('active');
        menuBtn.innerHTML = '☰';
    }
});

// 当前页面导航高亮
const sections = document.querySelectorAll('section[id]');
const navLinks = document.querySelectorAll('.nav-menu a[href^="#"]');

window.addEventListener('scroll', () => {
    let current = '';

    sections.forEach(section => {
        const sectionTop = section.offsetTop;
        const sectionHeight = section.clientHeight;
        if (window.pageYOffset >= sectionTop - 100) {
            current = section.getAttribute('id');
        }
    });

    navLinks.forEach(link => {
        link.style.color = '';
        if (link.getAttribute('href') === `#${current}`) {
            link.style.color = 'var(--primary-color)';
        }
    });
});

// 技术标签随机颜色
document.querySelectorAll('.tech-item').forEach(item => {
    item.addEventListener('mouseenter', () => {
        const colors = ['#1890ff', '#52c41a', '#faad14', '#f5222d', '#722ed1', '#eb2f96'];
        const randomColor = colors[Math.floor(Math.random() * colors.length)];
        item.style.background = randomColor;
    });

    item.addEventListener('mouseleave', () => {
        item.style.background = 'var(--bg-white)';
        item.style.color = 'var(--text-color)';
    });
});

// 页面加载完成后的初始化
window.addEventListener('load', () => {
    // 添加页面加载动画
    document.body.style.opacity = '0';
    document.body.style.transition = 'opacity 0.5s ease';

    setTimeout(() => {
        document.body.style.opacity = '1';
    }, 100);

    // 检查 URL hash，滚动到对应位置
    if (window.location.hash) {
        const target = document.querySelector(window.location.hash);
        if (target) {
            setTimeout(() => {
                const headerOffset = 80;
                const elementPosition = target.getBoundingClientRect().top;
                const offsetPosition = elementPosition + window.pageYOffset - headerOffset;
                window.scrollTo({
                    top: offsetPosition,
                    behavior: 'smooth'
                });
            }, 500);
        }
    }
});

// 性能优化：节流函数
function throttle(func, wait) {
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

// 使用节流优化滚动事件
window.addEventListener('scroll', throttle(() => {
    // 滚动事件处理逻辑
}, 100));

console.log('Winter 官网已加载完成 🎉');