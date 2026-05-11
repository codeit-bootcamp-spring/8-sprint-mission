// API Gateway 테스트 대시보드 JavaScript

class APIGatewayTester {
    constructor() {
        this.baseURL = '';
        this.testResults = document.getElementById('test-results');
        this.init();
    }

    init() {
        // 페이지 로드시 자동으로 헬스체크 실행
        this.checkAllServices();
    }

    // 로그 메시지 추가
    addLog(message, type = 'info') {
        const timestamp = new Date().toLocaleTimeString();
        const logEntry = document.createElement('div');
        logEntry.className = `log-entry ${type}`;
        
        logEntry.innerHTML = `
            <div class="log-timestamp">[${timestamp}]</div>
            <div class="log-message">${message}</div>
        `;
        
        // placeholder 제거
        const placeholder = this.testResults.querySelector('.placeholder');
        if (placeholder) {
            placeholder.remove();
        }
        
        this.testResults.appendChild(logEntry);
        this.testResults.scrollTop = this.testResults.scrollHeight;
    }

    // 결과 지우기
    clearResults() {
        this.testResults.innerHTML = '<p class="placeholder">테스트를 실행하면 결과가 여기에 표시됩니다.</p>';
    }

    // HTTP 요청 실행
    async makeRequest(url, method = 'GET', data = null) {
        const options = {
            method: method,
            headers: {
                'Content-Type': 'application/json',
            }
        };

        if (data && method !== 'GET') {
            options.body = JSON.stringify(data);
        }

        try {
            const response = await fetch(url, options);
            const responseData = await response.json();
            
            return {
                status: response.status,
                ok: response.ok,
                data: responseData,
                headers: Object.fromEntries(response.headers.entries())
            };
        } catch (error) {
            return {
                status: 0,
                ok: false,
                error: error.message
            };
        }
    }

    // 서비스 상태 업데이트
    updateServiceStatus(serviceId, isHealthy, message = '') {
        const statusElement = document.getElementById(`${serviceId}-status`);
        if (statusElement) {
            statusElement.className = `status-indicator ${isHealthy ? 'healthy' : 'unhealthy'}`;
            statusElement.textContent = isHealthy ? '정상' : '오류';
            
            if (message) {
                this.addLog(message, isHealthy ? 'success' : 'error');
            }
        }
    }

    // API Gateway 헬스체크
    async checkGatewayHealth() {
        this.addLog('🔍 API Gateway 헬스체크 시작...');
        
        const result = await this.makeRequest('/health');
        
        if (result.ok) {
            this.updateServiceStatus('gateway', true, '✅ API Gateway 정상 동작 중');
            return true;
        } else {
            this.updateServiceStatus('gateway', false, '❌ API Gateway 연결 실패');
            return false;
        }
    }

    // 개별 서비스 헬스체크
    async checkServiceHealth(serviceName) {
        this.addLog(`🔍 ${serviceName} 서비스 헬스체크 시작...`);
        
        const serviceMap = {
            'users': 'user',
            'orders': 'order', 
            'payments': 'payment'
        };
        
        const result = await this.makeRequest(`/api/${serviceName}/health`);
        
        if (result.ok && result.data.success) {
            const serviceType = serviceMap[serviceName];
            this.updateServiceStatus(serviceType, true, 
                `✅ ${result.data.data.service} 정상 동작 중 (응답시간: ${result.headers['x-response-time'] || 'N/A'})`
            );
            
            // 헬스체크 데이터 표시
            if (result.data.data) {
                this.addLog(`📊 서비스 정보: ${JSON.stringify(result.data.data, null, 2)}`);
            }
            
            return true;
        } else {
            const serviceType = serviceMap[serviceName];
            this.updateServiceStatus(serviceType, false, 
                `❌ ${serviceName} 서비스 연결 실패: ${result.error || result.data?.message || '알 수 없는 오류'}`
            );
            return false;
        }
    }

    // 모든 서비스 헬스체크
    async checkAllServices() {
        this.addLog('🚀 전체 서비스 헬스체크 시작...');
        
        await this.checkGatewayHealth();
        await this.checkServiceHealth('users');
        await this.checkServiceHealth('orders');
        await this.checkServiceHealth('payments');
        
        this.addLog('✅ 전체 헬스체크 완료');
    }

    // API 테스트 실행
    async testAPI(endpoint, method = 'GET', data = null) {
        this.addLog(`🧪 API 테스트: ${method} ${endpoint}`);
        
        const result = await this.makeRequest(endpoint, method, data);
        
        if (result.ok) {
            this.addLog(`✅ 성공 (${result.status}): ${endpoint}`, 'success');
            
            // 응답 헤더에서 API Gateway 정보 표시
            if (result.headers['x-gateway-service']) {
                this.addLog(`🎯 라우팅된 서비스: ${result.headers['x-gateway-service']}`);
            }
            
            if (result.headers['x-rate-limit']) {
                this.addLog(`⏱️ Rate Limit: ${result.headers['x-rate-limit']}`);
            }
            
            // 응답 데이터 표시 (요약)
            if (result.data && result.data.data) {
                const dataType = Array.isArray(result.data.data) ? 'array' : typeof result.data.data;
                const dataLength = Array.isArray(result.data.data) ? result.data.data.length : 1;
                this.addLog(`📋 응답 데이터: ${dataType} (${dataLength}개 항목)`);
            }
        } else {
            this.addLog(`❌ 실패 (${result.status}): ${endpoint} - ${result.error || result.data?.message || '알 수 없는 오류'}`, 'error');
        }
        
        return result;
    }

    // 사용자 생성 테스트
    async testUserCreate() {
        const userData = {
            name: `테스트사용자${Date.now()}`,
            email: `test${Date.now()}@example.com`,
            phone: '010-1234-5678'
        };
        
        await this.testAPI('/api/users', 'POST', userData);
    }

    // 주문 생성 테스트
    async testOrderCreate() {
        const orderData = {
            userId: 1,
            productName: `테스트상품${Date.now()}`,
            quantity: 1,
            unitPrice: 10000,
            deliveryAddress: '서울시 강남구 테스트동'
        };
        
        await this.testAPI('/api/orders', 'POST', orderData);
    }

    // 결제 생성 테스트
    async testPaymentCreate() {
        const paymentData = {
            orderId: 1,
            userId: 1,
            amount: 10000,
            method: 'CARD',
            cardLastFour: '1234'
        };
        
        await this.testAPI('/api/payments', 'POST', paymentData);
    }

    // Rate Limiting 스트레스 테스트
    async stressTest(serviceName, requestCount) {
        this.addLog(`🔥 Rate Limiting 테스트 시작: ${serviceName} (${requestCount}회 요청)`);
        
        const endpoint = `/api/${serviceName}`;
        let successCount = 0;
        let rateLimitCount = 0;
        let errorCount = 0;
        
        // 프로그레스 바 생성
        const progressContainer = document.createElement('div');
        progressContainer.className = 'progress-container';
        progressContainer.innerHTML = `
            <div class="progress-bar" style="width: 0%"></div>
        `;
        this.testResults.appendChild(progressContainer);
        
        const progressBar = progressContainer.querySelector('.progress-bar');
        
        // 병렬 요청 실행
        const requests = Array.from({length: requestCount}, (_, i) => 
            this.makeRequest(endpoint).then(result => {
                // 프로그레스 업데이트
                const progress = ((i + 1) / requestCount) * 100;
                progressBar.style.width = `${progress}%`;
                
                if (result.status === 429) {
                    rateLimitCount++;
                } else if (result.ok) {
                    successCount++;
                } else {
                    errorCount++;
                }
                
                return result;
            })
        );
        
        try {
            await Promise.all(requests);
            
            // 결과 요약
            this.addLog(`📈 Rate Limiting 테스트 완료:`, 'success');
            this.addLog(`   ✅ 성공: ${successCount}회`);
            this.addLog(`   🚫 Rate Limit: ${rateLimitCount}회`);
            this.addLog(`   ❌ 오류: ${errorCount}회`);
            
            if (rateLimitCount > 0) {
                this.addLog(`🎯 Rate Limiting이 정상적으로 작동하고 있습니다!`, 'success');
            } else {
                this.addLog(`⚠️ Rate Limiting이 예상대로 작동하지 않았습니다.`, 'warning');
            }
            
        } catch (error) {
            this.addLog(`❌ 스트레스 테스트 오류: ${error.message}`, 'error');
        } finally {
            // 프로그레스 바 제거
            setTimeout(() => {
                progressContainer.remove();
            }, 2000);
        }
    }
}

// 전역 함수들 (HTML에서 호출)
let tester;

document.addEventListener('DOMContentLoaded', function() {
    tester = new APIGatewayTester();
});

function checkGatewayHealth() {
    tester.checkGatewayHealth();
}

function checkServiceHealth(service) {
    tester.checkServiceHealth(service);
}

function testAPI(endpoint, method) {
    tester.testAPI(endpoint, method);
}

function testUserCreate() {
    tester.testUserCreate();
}

function testOrderCreate() {
    tester.testOrderCreate();
}

function testPaymentCreate() {
    tester.testPaymentCreate();
}

function stressTest(service, count) {
    tester.stressTest(service, count);
}

function clearResults() {
    tester.clearResults();
}
