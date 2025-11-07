package com.academy.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;
import org.springframework.core.io.Resource;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Swagger UI 커스터마이징 설정.
 */
@Configuration
public class SwaggerConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/")
                .resourceChain(false)
                .addResolver(new SwaggerIndexResourceResolver());
    }

    /**
     * Swagger UI index.html에 커스텀 JavaScript를 주입하는 ResourceResolver.
     */
    private static class SwaggerIndexResourceResolver implements ResourceResolver {

        private static final String SWAGGER_UI_INDEX_HTML = "index.html";
        
        @Override
        public Resource resolveResource(HttpServletRequest request, String requestPath,
                                      List<? extends Resource> locations, ResourceResolverChain chain) {
            
            Resource resolved = chain.resolveResource(request, requestPath, locations);
            
            if (resolved != null && SWAGGER_UI_INDEX_HTML.equals(requestPath)) {
                try {
                    return new SwaggerIndexHtmlResource(resolved);
                } catch (IOException e) {
                    // 실패 시 원본 리소스 반환
                    return resolved;
                }
            }
            
            return resolved;
        }

        @Override
        public String resolveUrlPath(String resourcePath, List<? extends Resource> locations,
                                   ResourceResolverChain chain) {
            return chain.resolveUrlPath(resourcePath, locations);
        }
    }

    /**
     * 커스텀 JavaScript가 주입된 Swagger UI HTML 리소스.
     */
    private static class SwaggerIndexHtmlResource extends ClassPathResource {
        
        private final Resource delegate;
        private static final String INDEX_HTML_PATH = "META-INF/resources/webjars/swagger-ui/index.html";
        
        public SwaggerIndexHtmlResource(Resource delegate) throws IOException {
            super(INDEX_HTML_PATH);
            this.delegate = delegate;
        }
        
        @Override
        public String getDescription() {
            return "Customized " + delegate.getDescription();
        }
        
        @Override
        public byte[] getContentAsByteArray() throws IOException {
            String originalContent = new String(delegate.getContentAsByteArray(), StandardCharsets.UTF_8);
            String customizedContent = injectCustomScript(originalContent);
            return customizedContent.getBytes(StandardCharsets.UTF_8);
        }
        
        private String injectCustomScript(String originalHtml) {
            String customScript = """
                <script>
                // Swagger UI 자동 토큰 설정 스크립트
                window.onload = function() {
                    console.log('🚀 자동 인증 스크립트 시작');

                    // 기존 토큰 확인 및 자동 설정
                    const savedToken = localStorage.getItem('swagger_access_token');
                    if (savedToken) {
                        setTimeout(() => setAuthToken(savedToken), 1000);
                    }
                    
                    // HTTP 요청 인터셉트
                    interceptRequests();
                };
                
                function setAuthToken(token) {
                    try {
                        if (window.ui && window.ui.authActions) {
                            window.ui.authActions.authorize({
                                bearerAuth: {
                                    name: 'bearerAuth',
                                    schema: { type: 'http', scheme: 'bearer' },
                                    value: token
                                }
                            });
                            localStorage.setItem('swagger_access_token', token);
                            console.log('✅ 토큰 자동 설정 완료');
                            showNotification('🎉 자동으로 인증 토큰이 설정되었습니다!', 'success');
                            updateAuthorizeButton(true);
                        }
                    } catch (error) {
                        console.error('토큰 설정 실패:', error);
                    }
                }
                
                function interceptRequests() {
                    const originalXHR = XMLHttpRequest.prototype.open;
                    XMLHttpRequest.prototype.open = function(method, url) {
                        this.addEventListener('load', function() {
                            if (method === 'POST' && url.includes('/api/auth/sign-in') && this.status === 200) {
                                try {
                                    const response = JSON.parse(this.responseText);
                                    if (response.success && response.data && response.data.accessToken) {
                                        console.log('🔑 로그인 성공 - 토큰 자동 설정');
                                        setAuthToken(response.data.accessToken);
                                    }
                                } catch (e) {
                                    console.error('로그인 응답 처리 실패:', e);
                                }
                            }
                        });
                        return originalXHR.apply(this, arguments);
                    };
                }
                
                function updateAuthorizeButton(isAuthorized) {
                    setTimeout(() => {
                        const btn = document.querySelector('.btn.authorize');
                        if (btn) {
                            if (isAuthorized) {
                                btn.style.background = '#49cc90';
                                btn.style.borderColor = '#49cc90';
                                btn.style.color = 'white';
                            } else {
                                btn.style.background = '';
                                btn.style.borderColor = '';
                                btn.style.color = '';
                            }
                        }
                    }, 100);
                }
                
                function showNotification(message, type) {
                    const notification = document.createElement('div');
                    notification.style.cssText = `
                        position: fixed; top: 20px; right: 20px; z-index: 9999;
                        padding: 12px 20px; border-radius: 5px; color: white;
                        background: ${type === 'success' ? '#49cc90' : '#ef4444'};
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
                        animation: slideIn 0.3s ease-out;
                    `;
                    notification.textContent = message;
                    document.body.appendChild(notification);
                    
                    setTimeout(() => notification.remove(), 3000);
                }
                
                // 전역 함수로 노출
                window.setSwaggerToken = setAuthToken;
                window.clearSwaggerToken = function() {
                    localStorage.removeItem('swagger_access_token');
                    if (window.ui && window.ui.authActions) {
                        window.ui.authActions.logout(['bearerAuth']);
                    }
                    updateAuthorizeButton(false);
                    showNotification('🔓 토큰이 제거되었습니다.', 'info');
                };
                
                console.log('🎯 자동 인증 스크립트 로드 완료');
                </script>
                <style>
                @keyframes slideIn {
                    from { transform: translateX(100%); opacity: 0; }
                    to { transform: translateX(0); opacity: 1; }
                }
                .btn.authorize.authorized {
                    background-color: #49cc90 !important;
                    border-color: #49cc90 !important;
                }
                </style>
                """;
            
            // </head> 태그 바로 앞에 커스텀 스크립트 주입
            return originalHtml.replace("</head>", customScript + "</head>");
        }
    }
}