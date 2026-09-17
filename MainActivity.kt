package com.darkhack

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private val registerUrl = "https://www.veergame77.com/#/register?invitationCode=39357662160"
    private val mainUrl = "https://www.veergame77.com/#/main"
    private val depositUrl = "https://www.veergame77.com/#/wallet/Recharge"
    private var retryCount = 0

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val webView = findViewById<WebView>(R.id.webView)

        // Smooth cookies for all devices
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true)
        }

        val s = webView.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.databaseEnabled = true
        s.allowFileAccess = true
        s.allowContentAccess = true
        s.loadsImagesAutomatically = true
        s.useWideViewPort = true
        s.loadWithOverviewMode = true
        s.setSupportZoom(false)
        s.builtInZoomControls = false
        s.displayZoomControls = false
        s.mediaPlaybackRequiresUserGesture = false
        s.javaScriptCanOpenWindowsAutomatically = true
        s.cacheMode = WebSettings.LOAD_DEFAULT
        // Smooth on low-end phones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            s.layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            s.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            s.safeBrowsingEnabled = false
        }
        s.userAgentString =
            "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

        try {
            webView.setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
        } catch (e: Exception) {
            webView.setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
        }

        webView.webChromeClient = WebChromeClient()
        webView.addJavascriptInterface(WebAppInterface(), "AndroidApp")
        playVoice()

        webView.webViewClient = object : WebViewClient() {

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true && retryCount < 3) {
                    retryCount++
                    Toast.makeText(this@MainActivity, "Retrying... ($retryCount)", Toast.LENGTH_SHORT).show()
                    view?.postDelayed({
                        view.loadUrl(view.url ?: registerUrl)
                    }, 1500L * retryCount)
                }
            }

            override fun onRenderProcessGone(
                view: WebView?,
                detail: android.webkit.RenderProcessGoneDetail?
            ): Boolean {
                try {
                    if (view != null) {
                        (view.parent as? android.view.ViewGroup)?.removeView(view)
                        view.destroy()
                    }
                } catch (_: Exception) {}
                recreate()
                return true
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                if (url.startsWith("http://") || url.startsWith("https://")) return false
                // tel / intent / market links
                return try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    true
                } catch (e: Exception) {
                    true
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                retryCount = 0
                try { CookieManager.getInstance().flush() } catch (_: Exception) {}

                val jsCode = """
                    javascript:(function(){
                        if(document.getElementById('dc-logo')||document.getElementById('dc-box'))return;
                        try{
                            // Force register if pure login page
                            setInterval(function(){
                                var u=(window.location.href||'').toLowerCase();
                                if(u.indexOf('login')>=0 && u.indexOf('register')<0){
                                    try{AndroidApp.playWarningVoice()}catch(e){}
                                    window.location.href='$registerUrl';
                                }
                            },1200);
                        }catch(e){}

                        var css=document.createElement('style');
                        css.innerHTML=`
                            #dc-logo{
                                position:fixed;width:58px;height:58px;border-radius:50%;
                                z-index:2147483646;cursor:grab;
                                display:flex;align-items:center;justify-content:center;
                                background:#0a0a0a;border:2.5px solid #ff1a3c;
                                box-shadow:0 0 16px #ff1a3c,0 0 32px rgba(255,20,50,.5);
                                animation:lp 2s infinite ease-in-out;
                                overflow:hidden;touch-action:none;user-select:none;-webkit-user-select:none;
                                transition:transform .2s;will-change:transform;
                            }
                            #dc-logo:active{cursor:grabbing;transform:scale(.88)}
                            #dc-logo .r{
                                position:absolute;inset:-5px;border-radius:50%;
                                border:2px solid transparent;border-top-color:#ff1a3c;border-bottom-color:#ff4466;
                                animation:sp 2s linear infinite;pointer-events:none;
                            }
                            #dc-logo img{width:100%;height:100%;object-fit:cover;border-radius:50%;display:block}
                            @keyframes lp{0%,100%{box-shadow:0 0 12px #ff1a3c}50%{box-shadow:0 0 24px #ff1a3c,0 0 40px rgba(255,20,50,.55)}}
                            @keyframes sp{to{transform:rotate(360deg)}}

                            #dc-box{
                                position:fixed;width:270px;
                                background:linear-gradient(165deg,#0a0608 0%,#120810 40%,#0a0610 100%);
                                border:1.5px solid rgba(255,30,60,.7);border-radius:14px;
                                box-shadow:0 0 24px rgba(255,20,50,.35),0 14px 44px rgba(0,0,0,.85);
                                z-index:2147483645;font-family:system-ui,-apple-system,sans-serif;
                                overflow:hidden;opacity:0;pointer-events:none;transform:scale(.78);
                                transition:opacity .28s ease,transform .35s cubic-bezier(.22,1,.36,1);
                                touch-action:none;user-select:none;-webkit-user-select:none;will-change:transform,opacity;
                            }
                            #dc-box.on{opacity:1;pointer-events:auto;transform:scale(1)}
                            .dc-g{position:absolute;inset:0;pointer-events:none;opacity:.3;
                                background-image:linear-gradient(rgba(255,30,60,.06) 1px,transparent 1px),linear-gradient(90deg,rgba(255,30,60,.06) 1px,transparent 1px);
                                background-size:14px 14px}
                            .dc-sc{position:absolute;left:0;right:0;height:2px;z-index:5;pointer-events:none;
                                background:linear-gradient(90deg,transparent,#ff1a3c,transparent);
                                animation:scm 2.5s linear infinite;opacity:.5}
                            @keyframes scm{0%{top:0;opacity:0}8%{opacity:.55}92%{opacity:.55}100%{top:100%;opacity:0}}

                            .dc-h{position:relative;z-index:2;display:flex;align-items:center;justify-content:space-between;
                                padding:10px 12px 8px;background:linear-gradient(180deg,rgba(255,30,60,.15),rgba(255,30,60,.03));
                                border-bottom:1px solid rgba(255,30,60,.3);cursor:grab}
                            .dc-h:active{cursor:grabbing}
                            .dc-hl{display:flex;align-items:center;gap:6px}
                            .dc-d{width:8px;height:8px;border-radius:50%;background:#ff1a3c;box-shadow:0 0 10px #ff1a3c;animation:bl 1s infinite alternate}
                            @keyframes bl{0%{opacity:.25}100%{opacity:1}}
                            .dc-t{color:#ff4466;font-weight:900;font-size:10px;letter-spacing:.8px;text-transform:uppercase;text-shadow:0 0 12px rgba(255,30,60,.7)}
                            .dc-tg{background:rgba(255,30,60,.2);border:1px solid #ff1a3c;color:#ff4466;font-size:8px;font-weight:800;padding:2px 7px;border-radius:4px}
                            .dc-tg.live{background:rgba(0,255,136,.15);border-color:#00ff88;color:#00ff88}

                            .dc-bd{position:relative;z-index:2;display:flex}
                            .dc-lv{display:flex;flex-direction:column;align-items:center;justify-content:center;gap:5px;padding:12px 7px;border-right:1px solid rgba(255,30,60,.18)}
                            .dc-lv i{width:17px;height:17px;border-radius:50%;background:rgba(255,30,60,.15);border:1.3px solid #ff1a3c;
                                color:#ff4466;font-size:8px;font-weight:900;font-style:normal;display:flex;align-items:center;justify-content:center}
                            .dc-ct{flex:1;padding:14px 11px 11px;text-align:center}

                            #dc-lk .lb{color:#ff6680;font-size:8px;font-weight:700;letter-spacing:2px;margin-bottom:8px}
                            #dc-lk .lock-ico{
                                width:42px;height:42px;margin:0 auto 10px;border-radius:50%;
                                background:radial-gradient(circle at 35% 30%,#2a0a12,#0a0408);
                                border:2px solid #ff1a3c;box-shadow:0 0 18px rgba(255,30,60,.5);
                                display:flex;align-items:center;justify-content:center;font-size:20px;
                                animation:lockP 2s infinite ease-in-out;
                            }
                            @keyframes lockP{0%,100%{box-shadow:0 0 12px rgba(255,30,60,.4)}50%{box-shadow:0 0 24px rgba(255,30,60,.75)}}
                            #dc-lk .bg{color:#ff1a3c;font-size:15px;font-weight:900;letter-spacing:1.5px;margin-bottom:6px;text-shadow:0 0 14px rgba(255,30,60,.7)}
                            #dc-lk .br{width:60%;height:3px;margin:0 auto 7px;border-radius:3px;background:linear-gradient(90deg,#ff1a3c,#ff6680,transparent);animation:ba 1.4s infinite linear}
                            @keyframes ba{0%{opacity:.25;transform:scaleX(.35)}50%{opacity:1;transform:scaleX(1)}100%{opacity:.25;transform:scaleX(.35)}}
                            #dc-lk .su{color:#ff6680;font-size:8px;font-weight:700;letter-spacing:1.2px;margin-bottom:4px}
                            #dc-lk .dep-hint{color:#ffaa00;font-size:9px;font-weight:800;margin-bottom:12px;text-shadow:0 0 8px rgba(255,170,0,.4)}

                            #dc-un{display:none}
                            #dc-un .badge{display:inline-block;background:linear-gradient(90deg,rgba(255,30,60,.25),rgba(255,30,60,.06));
                                border:1px solid rgba(255,30,60,.5);border-radius:20px;color:#ff4466;font-size:8px;font-weight:800;
                                letter-spacing:1.5px;padding:3px 11px;margin-bottom:8px}
                            #dc-un .rs{font-size:32px;font-weight:900;letter-spacing:3px;line-height:1;margin-bottom:8px;text-transform:uppercase}
                            #dc-un .ns{display:flex;justify-content:center;gap:9px;margin-bottom:8px}
                            #dc-un .bl{width:32px;height:32px;border-radius:50%;display:flex;align-items:center;justify-content:center;
                                font-size:14px;font-weight:900;font-family:monospace;background:rgba(0,0,0,.6);border:2px solid}
                            #dc-un .pr{color:#ff6680;font-size:10px;font-weight:700;margin-bottom:3px}
                            #dc-un .dv{color:#ffaa00;font-size:9px;font-weight:800;letter-spacing:.7px;margin-bottom:8px}

                            .dc-bs{display:flex;gap:7px}
                            .dc-lg{flex:1.35;padding:10px 0;background:linear-gradient(90deg,#cc1030,#ff1a3c);color:#fff;
                                font-weight:900;font-size:11px;letter-spacing:1.3px;border:none;border-radius:8px;cursor:pointer;text-transform:uppercase;
                                box-shadow:0 4px 16px rgba(255,30,60,.5)}
                            .dc-lg:active{opacity:.88;transform:scale(.96)}
                            .dc-hd{flex:.7;padding:10px 0;background:transparent;color:#ff4466;font-weight:800;font-size:10px;
                                letter-spacing:1px;border:1.4px solid #ff1a3c;border-radius:8px;cursor:pointer;text-transform:uppercase}
                            .dc-hd:active{background:rgba(255,30,60,.12)}

                            .dc-f{position:relative;z-index:2;display:flex;justify-content:space-between;align-items:center;
                                padding:6px 11px;border-top:1px solid rgba(255,30,60,.2);background:rgba(0,0,0,.45)}
                            .dc-o{color:#ff4466;font-size:8px;font-weight:700}
                            .dc-o.live{color:#00ff88}
                            .dc-o::before{content:'';display:inline-block;width:6px;height:6px;border-radius:50%;
                                background:#ff1a3c;box-shadow:0 0 6px #ff1a3c;margin-right:4px;vertical-align:middle;animation:bl 1s infinite alternate}
                            .dc-o.live::before{background:#00ff88;box-shadow:0 0 6px #00ff88}
                            .dc-m{color:#664;font-size:8px}
                        `;
                        document.head.appendChild(css);

                        var logo=document.createElement('div');
                        logo.id='dc-logo';
                        logo.innerHTML='<div class="r"></div><img src="data:image/png;base64,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" alt="">';
                        document.body.appendChild(logo);

                        var box=document.createElement('div');
                        box.id='dc-box';
                        box.innerHTML=`
                            <div class="dc-g"></div><div class="dc-sc"></div>
                            <div class="dc-h" id="dc-hd">
                                <div class="dc-hl"><div class="dc-d"></div><div class="dc-t">Hack with akaay bhai</div></div>
                                <div class="dc-tg" id="dc-tag">LOCKED</div>
                            </div>
                            <div class="dc-bd">
                                <div class="dc-lv"><i>L</i><i>I</i><i>V</i><i>E</i></div>
                                <div class="dc-ct">
                                    <div id="dc-lk">
                                        <div class="lb">ACCESS DENIED</div>
                                        <div class="lock-ico">🔒</div>
                                        <div class="bg">SYSTEM LOCKED</div>
                                        <div class="br"></div>
                                        <div class="su">DEPOSIT TO UNLOCK HACK</div>
                                        <div class="dep-hint">⚡ MIN ₹300 REQUIRED</div>
                                        <div class="dc-bs">
                                            <button class="dc-lg" id="dc-lg">DEPOSIT</button>
                                            <button class="dc-hd" id="dc-hb">HIDE</button>
                                        </div>
                                    </div>
                                    <div id="dc-un">
                                        <div class="badge">● PREDICTION LIVE</div>
                                        <div class="rs" id="dc-rs" style="color:#555">WAIT</div>
                                        <div class="ns" id="dc-ns">
                                            <div class="bl" style="border-color:#555;color:#555">-</div>
                                            <div class="bl" style="border-color:#555;color:#555">-</div>
                                            <div class="bl" style="border-color:#555;color:#555">-</div>
                                        </div>
                                        <div class="pr">Period : <span id="dc-pr">...</span></div>
                                        <div class="dv">DEV : DARK CYBER</div>
                                        <div class="dc-bs"><button class="dc-hd" id="dc-hb2" style="flex:1">HIDE</button></div>
                                    </div>
                                </div>
                            </div>
                            <div class="dc-f">
                                <div class="dc-o" id="dc-on">CORE LOCKED</div>
                                <div class="dc-m" id="dc-pg">38 ms</div>
                            </div>
                        `;
                        document.body.appendChild(box);

                        var open=false,ld=false,lm=false,pd=false;
                        var lsx=0,lsy=0,llx=0,lly=0,lox=0,loy=0,plx=0,ply=0,pox=0,poy=0;

                        function sL(x,y){
                            llx=Math.max(0,Math.min(x,window.innerWidth-58));
                            lly=Math.max(0,Math.min(y,window.innerHeight-58));
                            logo.style.left=llx+'px';logo.style.top=lly+'px';
                        }
                        sL(window.innerWidth-74,window.innerHeight-150);

                        function sP(x,y){
                            var w=270,h=box.offsetHeight||260;
                            plx=Math.max(0,Math.min(x,window.innerWidth-w));
                            ply=Math.max(0,Math.min(y,window.innerHeight-h));
                            box.style.left=plx+'px';box.style.top=ply+'px';
                            box.style.right='auto';box.style.bottom='auto';box.style.transform='scale(1)';
                        }

                        function openBox(){
                            if(open)return;open=true;
                            logo.style.transition='transform .2s,opacity .2s';
                            logo.style.transform='scale(0)';logo.style.opacity='0';
                            plx=(window.innerWidth-270)/2;ply=Math.max(20,(window.innerHeight-280)/2);
                            box.style.left=plx+'px';box.style.top=ply+'px';
                            box.style.right='auto';box.style.bottom='auto';
                            setTimeout(function(){box.classList.add('on');try{AndroidApp.showToast('⚡ Hack with akaay bhai')}catch(e){}},160);
                        }
                        function closeBox(){
                            box.classList.remove('on');
                            setTimeout(function(){
                                open=false;
                                logo.style.transform='scale(1)';logo.style.opacity='1';
                                logo.style.animation='lp 2s infinite ease-in-out';
                            },260);
                        }

                        document.getElementById('dc-hb').onclick=function(e){e.stopPropagation();closeBox()};
                        document.getElementById('dc-hb2').onclick=function(e){e.stopPropagation();closeBox()};
                        document.getElementById('dc-lg').onclick=function(e){
                            e.stopPropagation();
                            try{AndroidApp.playDepositClickVoice()}catch(err){}
                            closeBox();
                            setTimeout(function(){window.location.href='${depositUrl}'},260);
                        };

                        logo.addEventListener('touchstart',function(e){
                            if(open)return;e.stopPropagation();ld=true;lm=false;
                            var t=e.touches[0];lsx=t.clientX;lsy=t.clientY;lox=t.clientX-llx;loy=t.clientY-lly;
                            logo.style.transition='none';logo.style.animation='none';
                        },{passive:false});
                        logo.addEventListener('touchmove',function(e){
                            if(!ld||open)return;e.preventDefault();
                            var t=e.touches[0];
                            if(Math.abs(t.clientX-lsx)>6||Math.abs(t.clientY-lsy)>6)lm=true;
                            sL(t.clientX-lox,t.clientY-loy);
                        },{passive:false});
                        logo.addEventListener('touchend',function(){
                            if(open)return;ld=false;
                            if(!lm)openBox();else logo.style.animation='lp 2s infinite ease-in-out';
                        });

                        var hd=document.getElementById('dc-hd');
                        hd.addEventListener('touchstart',function(e){
                            if(!open)return;e.stopPropagation();pd=true;
                            var t=e.touches[0];pox=t.clientX-plx;poy=t.clientY-ply;box.style.transition='none';
                        },{passive:false});
                        document.addEventListener('touchmove',function(e){
                            if(!pd||!open)return;e.preventDefault();
                            var t=e.touches[0];sP(t.clientX-pox,t.clientY-poy);
                        },{passive:false});
                        document.addEventListener('touchend',function(){
                            if(!pd)return;pd=false;
                            box.style.transition='opacity .28s ease,transform .35s cubic-bezier(.22,1,.36,1)';
                        });

                        function doUnlock(){
                            document.getElementById('dc-lk').style.display='none';
                            document.getElementById('dc-un').style.display='block';
                            var tag=document.getElementById('dc-tag');
                            tag.innerText='LIVE';tag.classList.add('live');
                            var on=document.getElementById('dc-on');
                            on.innerText='CORE ONLINE';on.classList.add('live');
                        }

                        var unlocked=false;
                        try{unlocked=localStorage.getItem('hack_unlocked_status')==='true'}catch(e){}
                        var initB=-1,sc=0;
                        try{
                            var ib=localStorage.getItem('init_wallet_balance');
                            if(ib!==null)initB=parseFloat(ib);
                        }catch(e){}
                        if(unlocked) doUnlock();

                        function getBal(){
                            try{
                                var t=document.body.innerText||'';
                                var m=t.match(/(?:Total balance|Balance|Wallet)[\\s\\S]{0,40}?₹\\s*([0-9.,]+)/i);
                                if(m)return parseFloat(m[1].replace(/,/g,''));
                                var a=t.match(/₹\\s*([0-9.,]+)/g);
                                if(a){
                                    for(var i=0;i<a.length;i++){
                                        var v=parseFloat(a[i].replace(/[^\\d.]/g,''));
                                        if(v>0&&v!==100&&v!==500&&v!==1000&&v!==5000&&v!==300)return v;
                                    }
                                }
                            }catch(e){}
                            return -1;
                        }
                        function scan(){
                            if(unlocked)return;
                            try{
                                var b=getBal();
                                var u=(window.location.href||'').toLowerCase();
                                if(u.indexOf('recharge')>=0||u.indexOf('deposit')>=0||u.indexOf('pay')>=0)return;
                                if(b>=0){
                                    sc++;
                                    if(initB===-1){if(sc>3){initB=b;try{localStorage.setItem('init_wallet_balance',String(b))}catch(e){}}}
                                    else{
                                        if(b-initB>=300){
                                            unlocked=true;
                                            try{localStorage.setItem('hack_unlocked_status','true')}catch(e){}
                                            doUnlock();
                                            try{AndroidApp.playDepositVoice()}catch(e){}
                                            try{AndroidApp.showToast('Payment Verified! Hack Unlocked.')}catch(e){}
                                        }else if(b<initB){
                                            initB=b;
                                            try{localStorage.setItem('init_wallet_balance',String(b))}catch(e){}
                                        }
                                    }
                                }
                            }catch(e){}
                        }
                        setInterval(scan,1800);

                        var L={0:{n:'5/9/7',s:'BIG',c:'#ff1a3c'},1:{n:'0/2/4',s:'SMALL',c:'#00ff88'},2:{n:'0/7/4',s:'SMALL',c:'#00ff88'},3:{n:'7/1/9',s:'BIG',c:'#ff1a3c'},4:{n:'6/8/2',s:'BIG',c:'#ff1a3c'},5:{n:'5/9/3',s:'BIG',c:'#ff1a3c'},6:{n:'5/7/0',s:'BIG',c:'#ff1a3c'},7:{n:'0/2/8',s:'SMALL',c:'#00ff88'},8:{n:'4/6/1',s:'SMALL',c:'#00ff88'},9:{n:'1/3/9',s:'SMALL',c:'#00ff88'}};
                        var lp=null;
                        function fb(){var d=new Date();return d.getFullYear()+String(d.getMonth()+1).padStart(2,'0')+String(d.getDate()).padStart(2,'0')+'1'+String(d.getHours()*60+d.getMinutes()+1).padStart(4,'0')}
                        function ap(p){
                            if(p===lp)return;lp=p;
                            try{
                                var el=document.getElementById('dc-pr');if(el)el.innerText=p;
                                var pr=L[parseInt(p.slice(-1))]||L[0];
                                var rs=document.getElementById('dc-rs');
                                if(rs){rs.innerText=pr.s;rs.style.color=pr.c;rs.style.textShadow='0 0 16px '+pr.c}
                                var h='';pr.n.split('/').forEach(function(n){
                                    h+='<div class="bl" style="border-color:'+pr.c+';color:'+pr.c+';box-shadow:0 0 12px '+pr.c+'">'+n+'</div>';
                                });
                                var ns=document.getElementById('dc-ns');if(ns)ns.innerHTML=h;
                            }catch(e){}
                        }
                        function fp(){
                            try{
                                var payload = {
                                    typeId: 1,
                                    language: 0,
                                    random: "40079dcba93a48769c6ee9d4d4fae23f",
                                    signature: "D12108C4F57C549D82B23A91E0FA20AE",
                                    timestamp: Math.floor(Date.now() / 1000)
                                };
                                fetch('https://api.bdg88zf.com/api/webapi/GetGameIssue', {
                                    method: 'POST',
                                    headers: { 'Content-Type': 'application/json' },
                                    body: JSON.stringify(payload)
                                }).then(function(r){ return r.json(); }).then(function(d){
                                    if(d && d.data && d.data.issueNumber) ap(String(d.data.issueNumber));
                                    else ap(fb());
                                }).catch(function(){ ap(fb()); });
                            }catch(e){ ap(fb()); }
                        }
                        setInterval(fp,3000);fp();
                        setInterval(function(){
                            try{var p=document.getElementById('dc-pg');if(p)p.innerText=(28+Math.floor(Math.random()*30))+' ms'}catch(e){}
                        },2000);
                    })();
                """.trimIndent()

                try {
                    view?.evaluateJavascript(jsCode, null)
                } catch (e: Exception) {
                    view?.loadUrl(jsCode)
                }
            }
        }

        // Clean load
        try {
            webView.clearCache(false)
        } catch (_: Exception) {}
        webView.loadUrl(registerUrl)
    }

    override fun onPause() {
        super.onPause()
        try { CookieManager.getInstance().flush() } catch (_: Exception) {}
    }

    override fun onResume() {
        super.onResume()
        try {
            findViewById<WebView>(R.id.webView).onResume()
        } catch (_: Exception) {}
    }

    fun playVoice() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, R.raw.my_voice)
            mediaPlayer?.start()
        } catch (_: Exception) {}
    }

    fun playDepositVoice() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, R.raw.deposit_voice)
            mediaPlayer?.start()
        } catch (_: Exception) {}
    }

    fun playDepositClickAudio() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, R.raw.deposit_voice)
            mediaPlayer?.start()
        } catch (_: Exception) {}
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun playWarningVoice() {
            runOnUiThread { playVoice() }
        }

        @JavascriptInterface
        fun playDepositVoice() {
            runOnUiThread { this@MainActivity.playDepositVoice() }
        }

        @JavascriptInterface
        fun playDepositClickVoice() {
            runOnUiThread { this@MainActivity.playDepositClickAudio() }
        }

        @JavascriptInterface
        fun showToast(msg: String) {
            runOnUiThread {
                Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        val webView = findViewById<WebView>(R.id.webView)
        val currentUrl = webView.url?.lowercase() ?: ""
        if (currentUrl.contains("register") || currentUrl.contains("login")) {
            Toast.makeText(this, "Please register to continue", Toast.LENGTH_SHORT).show()
            return
        }
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
