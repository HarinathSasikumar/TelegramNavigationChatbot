package com.navigation.chatbot.controller;

import com.navigation.chatbot.model.*;
import com.navigation.chatbot.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1")
public class ApiController {

    private static final Logger log = LoggerFactory.getLogger(ApiController.class);

    private final UserSessionRepository sessionRepository;
    private final NavigationRequestRepository navigationRepository;

    public ApiController(UserSessionRepository sessionRepository,
                         NavigationRequestRepository navigationRepository) {
        this.sessionRepository = sessionRepository;
        this.navigationRepository = navigationRepository;
    }

    /**
     * Health dashboard — uses DeferredResult so the Firestore calls (which can
     * take a few seconds) never block the servlet thread and never cause
     * AsyncRequestNotUsableException / ERR_EMPTY_RESPONSE.
     */
    @GetMapping(value = "/health", produces = MediaType.TEXT_HTML_VALUE)
    public DeferredResult<ResponseEntity<String>> health() {
        // 10-second timeout — if Firestore is still blocked, show setup page
        DeferredResult<ResponseEntity<String>> deferred = new DeferredResult<>(10_000L,
                ResponseEntity.ok(getSetupRequiredHtml()));

        if (!sessionRepository.isConfigured()) {
            deferred.setResult(ResponseEntity.ok(getSetupRequiredHtml()));
            return deferred;
        }

        CompletableFuture.supplyAsync(() -> {
            long users = 0;
            long messages = 0;
            try {
                users = sessionRepository.getTotalUsersCount();
                messages = sessionRepository.getTotalMessagesCount();
            } catch (Exception e) {
                log.warn("Health check Firebase fetch failed: {}", e.getMessage());
            }
            return new long[]{users, messages};
        }).orTimeout(8, TimeUnit.SECONDS)
          .whenComplete((data, err) -> {
              if (err != null || data == null) {
                  log.warn("Health check timed out or failed: {}",
                          err != null ? err.getMessage() : "null data");
                  deferred.setResult(ResponseEntity.ok(getSetupRequiredHtml()));
              } else {
                  deferred.setResult(ResponseEntity.ok(getHealthHtml(data[0], data[1])));
              }
          });

        return deferred;
    }

    private String getSetupRequiredHtml() {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>System Setup | Nexus AI</title>
                <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;700&display=swap" rel="stylesheet">
                <style>
                    :root {
                        --bg-base: #050505;
                        --glass-bg: rgba(20, 20, 25, 0.4);
                        --glass-border: rgba(255, 255, 255, 0.08);
                        --warning: #ff4757;
                        --warning-glow: rgba(255, 71, 87, 0.2);
                        --text-main: #ffffff;
                        --text-muted: #8b8b93;
                    }

                    * { margin: 0; padding: 0; box-sizing: border-box; }

                    body {
                        font-family: 'Outfit', sans-serif;
                        background-color: var(--bg-base);
                        color: var(--text-main);
                        min-height: 100vh;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        overflow: hidden;
                    }

                    .ambient-bg {
                        position: fixed;
                        top: 0; left: 0; right: 0; bottom: 0;
                        z-index: -1;
                        background: radial-gradient(circle at 50% 50%, var(--warning-glow), transparent 40%);
                        filter: blur(80px);
                        animation: pulse-bg 4s ease-in-out infinite alternate;
                    }

                    @keyframes pulse-bg {
                        0% { opacity: 0.6; }
                        100% { opacity: 1; }
                    }

                    .container {
                        position: relative;
                        width: 90%;
                        max-width: 600px;
                        padding: 3.5rem;
                        background: var(--glass-bg);
                        backdrop-filter: blur(24px);
                        -webkit-backdrop-filter: blur(24px);
                        border: 1px solid var(--glass-border);
                        border-radius: 24px;
                        box-shadow: 0 30px 60px rgba(0, 0, 0, 0.4);
                        text-align: center;
                    }

                    .badge {
                        display: inline-flex;
                        align-items: center;
                        padding: 8px 16px;
                        background: rgba(255, 71, 87, 0.1);
                        border: 1px solid rgba(255, 71, 87, 0.2);
                        border-radius: 30px;
                        font-size: 0.8rem;
                        font-weight: 700;
                        letter-spacing: 1px;
                        color: var(--warning);
                        text-transform: uppercase;
                        margin-bottom: 2rem;
                    }

                    h1 {
                        font-size: 2.2rem;
                        font-weight: 800;
                        margin-bottom: 1rem;
                        background: linear-gradient(135deg, #fff 0%, #a5a5b0 100%);
                        -webkit-background-clip: text;
                        -webkit-text-fill-color: transparent;
                    }

                    p.desc {
                        color: var(--text-muted);
                        margin-bottom: 2.5rem;
                        line-height: 1.6;
                        font-size: 1.05rem;
                    }

                    .steps {
                        text-align: left;
                        background: rgba(0, 0, 0, 0.3);
                        padding: 2rem;
                        border-radius: 16px;
                        border: 1px solid rgba(255,255,255,0.03);
                    }

                    .step {
                        display: flex;
                        align-items: flex-start;
                        margin-bottom: 1.5rem;
                    }
                    .step:last-child { margin-bottom: 0; }

                    .step-num {
                        background: linear-gradient(135deg, var(--warning), #ff6b81);
                        color: #fff;
                        width: 28px;
                        height: 28px;
                        border-radius: 50%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-weight: 800;
                        font-size: 0.9rem;
                        margin-right: 1.2rem;
                        flex-shrink: 0;
                        box-shadow: 0 4px 10px var(--warning-glow);
                    }

                    .step-text { color: #d0d0d5; font-size: 1rem; line-height: 1.5; }
                    .code { 
                        font-family: monospace; 
                        background: rgba(255,255,255,0.08); 
                        padding: 0.2rem 0.5rem; 
                        border-radius: 4px;
                        color: var(--warning);
                    }
                </style>
            </head>
            <body>
                <div class="ambient-bg"></div>
                <div class="container">
                    <div class="badge">Configuration Required</div>
                    <h1>Database Connection Lost</h1>
                    <p class="desc">The Chatbot is currently unable to reach Firestore. Advanced navigation features require a valid database connection to operate.</p>
                    
                    <div class="steps">
                        <div class="step">
                            <div class="step-num">1</div>
                            <div class="step-text">Download your Service Account JSON from standard <b>Firebase Console</b>.</div>
                        </div>
                        <div class="step">
                            <div class="step-num">2</div>
                            <div class="step-text">Rename the configuration file to <span class="code">firebase-service-account.json</span>.</div>
                        </div>
                        <div class="step">
                            <div class="step-num">3</div>
                            <div class="step-text">Place the JSON file securely in the <b>root directory</b> of this project.</div>
                        </div>
                        <div class="step">
                            <div class="step-num">4</div>
                            <div class="step-text"><b>Reboot the core system</b> to securely authenticate and apply.</div>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """;
    }

    private String getHealthHtml(long users, long messages) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Nexus Hub | Diagnostics</title>
                <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;700;800&display=swap" rel="stylesheet">
                <style>
                    :root {
                        --bg-base: #030305;
                        --glass-bg: rgba(20, 20, 25, 0.4);
                        --glass-border: rgba(255, 255, 255, 0.08);
                        --primary: #00f2fe;
                        --secondary: #4facfe;
                        --accent: #7f00ff;
                        --text-main: #ffffff;
                        --text-muted: #8b8b93;
                    }

                    * { margin: 0; padding: 0; box-sizing: border-box; }

                    body {
                        font-family: 'Outfit', sans-serif;
                        background-color: var(--bg-base);
                        color: var(--text-main);
                        min-height: 100vh;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        overflow: hidden;
                        perspective: 1000px;
                    }

                    .ambient-bg {
                        position: fixed;
                        top: 0; left: 0; right: 0; bottom: 0;
                        z-index: -1;
                        background: 
                            radial-gradient(circle at 15%% 50%%, rgba(79, 172, 254, 0.15), transparent 25%%),
                            radial-gradient(circle at 85%% 30%%, rgba(127, 0, 255, 0.15), transparent 25%%);
                        filter: blur(80px);
                        animation: pulse-bg 10s ease-in-out infinite alternate;
                    }

                    @keyframes pulse-bg {
                        0%% { transform: scale(1); opacity: 0.8; }
                        100%% { transform: scale(1.1); opacity: 1; }
                    }

                    .dashboard-container {
                        position: relative;
                        width: 90%%;
                        max-width: 720px;
                        padding: 3.5rem;
                        background: var(--glass-bg);
                        backdrop-filter: blur(24px);
                        -webkit-backdrop-filter: blur(24px);
                        border: 1px solid var(--glass-border);
                        border-radius: 24px;
                        box-shadow: 0 30px 60px rgba(0, 0, 0, 0.4), inset 0 1px 0 rgba(255, 255, 255, 0.1);
                        transform-style: preserve-3d;
                        transition: transform 0.1s;
                    }

                    .status-header {
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                        margin-bottom: 3rem;
                        position: relative;
                    }

                    .system-badge {
                        display: flex;
                        align-items: center;
                        gap: 10px;
                        padding: 8px 16px;
                        background: rgba(0, 242, 254, 0.1);
                        border: 1px solid rgba(0, 242, 254, 0.2);
                        border-radius: 30px;
                        font-size: 0.8rem;
                        font-weight: 700;
                        letter-spacing: 1px;
                        color: var(--primary);
                        text-transform: uppercase;
                        box-shadow: 0 0 20px rgba(0, 242, 254, 0.1);
                    }

                    .live-dot {
                        width: 8px; height: 8px;
                        background: var(--primary);
                        border-radius: 50%%;
                        box-shadow: 0 0 10px var(--primary);
                        animation: blink 2s infinite;
                    }

                    @keyframes blink { 0%%, 100%% { opacity: 1; } 50%% { opacity: 0.4; } }

                    .title-group h1 {
                        font-size: 2.6rem;
                        font-weight: 800;
                        background: linear-gradient(135deg, #fff 0%%, #a5a5b0 100%%);
                        -webkit-background-clip: text;
                        -webkit-text-fill-color: transparent;
                        margin-bottom: 0.5rem;
                        letter-spacing: -0.5px;
                    }

                    .title-group p {
                        color: var(--text-muted);
                        font-size: 1.1rem;
                        font-weight: 400;
                    }

                    .metrics-grid {
                        display: grid;
                        grid-template-columns: repeat(2, 1fr);
                        gap: 1.5rem;
                        margin-bottom: 2.5rem;
                    }

                    .metric-card {
                        background: rgba(0, 0, 0, 0.2);
                        border: 1px solid rgba(255, 255, 255, 0.05);
                        padding: 2.5rem 2rem;
                        border-radius: 20px;
                        position: relative;
                        overflow: hidden;
                        transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
                    }

                    .metric-card:hover {
                        transform: translateY(-8px) scale(1.02);
                        border-color: rgba(255, 255, 255, 0.15);
                        background: rgba(255, 255, 255, 0.04);
                        box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4);
                    }

                    .metric-card::before {
                        content: '';
                        position: absolute;
                        top: 0; left: 0;
                        width: 100%%; height: 3px;
                        background: linear-gradient(90deg, var(--secondary), var(--accent));
                        opacity: 0;
                        transition: opacity 0.3s;
                    }

                    .metric-card:hover::before { opacity: 1; }

                    .metric-label {
                        color: var(--text-muted);
                        font-size: 0.85rem;
                        font-weight: 600;
                        text-transform: uppercase;
                        letter-spacing: 1.5px;
                        margin-bottom: 1rem;
                        display: flex;
                        align-items: center;
                        gap: 10px;
                    }

                    .metric-value {
                        font-size: 4rem;
                        font-weight: 800;
                        color: #fff;
                        line-height: 1;
                        text-shadow: 0 4px 20px rgba(255, 255, 255, 0.1);
                        background: linear-gradient(135deg, #fff 0%%, #d4d4d8 100%%);
                        -webkit-background-clip: text;
                        -webkit-text-fill-color: transparent;
                    }

                    .footer {
                        text-align: center;
                        padding-top: 2rem;
                        border-top: 1px solid var(--glass-border);
                        color: rgba(255, 255, 255, 0.3);
                        font-size: 0.85rem;
                        letter-spacing: 0.5px;
                    }
                </style>
            </head>
            <body>
                <div class="ambient-bg"></div>
                <div class="dashboard-container" id="dashboard">
                    <div class="status-header">
                        <div class="title-group">
                            <h1>Nexus Core Hub</h1>
                            <p>Real-time analytics & intelligence routing</p>
                        </div>
                        <div class="system-badge">
                            <div class="live-dot"></div>
                            Operational
                        </div>
                    </div>

                    <div class="metrics-grid">
                        <div class="metric-card">
                            <div class="metric-label">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
                                Active Users
                            </div>
                            <div class="metric-value counter" data-target="%d">0</div>
                        </div>
                        <div class="metric-card">
                            <div class="metric-label">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>
                                Transmissions
                            </div>
                            <div class="metric-value counter" data-target="%d">0</div>
                        </div>
                    </div>

                    <div class="footer">
                        &copy; 2026 Nexus AI Architecture &bull; v2.0.0 Enterprise
                    </div>
                </div>

                <script>
                    const counters = document.querySelectorAll('.counter');
                    counters.forEach(counter => {
                        const target = +counter.getAttribute('data-target');
                        if (target > 0) {
                            let count = 0;
                            const speed = target > 1000 ? 50 : 20;
                            const inc = target / speed;
                            
                            const updateCount = () => {
                                count += inc;
                                if (count < target) {
                                    counter.innerText = Math.ceil(count);
                                    requestAnimationFrame(updateCount);
                                } else {
                                    counter.innerText = target;
                                }
                            };
                            updateCount();
                        } else {
                            counter.innerText = target;
                        }
                    });

                    const dashboard = document.getElementById('dashboard');
                    document.addEventListener('mousemove', (e) => {
                        const xAxis = (window.innerWidth / 2 - e.pageX) / 45;
                        const yAxis = (window.innerHeight / 2 - e.pageY) / 45;
                        dashboard.style.transform = `rotateY(${xAxis}deg) rotateX(${yAxis}deg)`;
                    });
                    
                    document.addEventListener('mouseleave', () => {
                        dashboard.style.transform = `rotateY(0deg) rotateX(0deg)`;
                    });
                </script>
            </body>
            </html>
            """.formatted(users, messages);
    }

    @GetMapping("/sessions/{phoneNumber}")
    public ResponseEntity<?> getSession(@PathVariable String phoneNumber) {
        Optional<UserSession> session = sessionRepository.findByPhoneNumber(phoneNumber);
        return session.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/navigation/{phoneNumber}")
    public ResponseEntity<List<NavigationRequest>> getNavigationHistory(@PathVariable String phoneNumber) {
        return ResponseEntity.ok(navigationRepository.findByPhoneNumber(phoneNumber));
    }

    @DeleteMapping("/sessions/{phoneNumber}")
    public ResponseEntity<String> deleteSession(@PathVariable String phoneNumber) {
        sessionRepository.delete(phoneNumber);
        return ResponseEntity.ok("Session deleted for: " + phoneNumber);
    }
}
