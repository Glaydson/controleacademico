<#macro registrationLayout bodyClass="" displayInfo=false displayMessage=true displayRequiredFields=false showAnotherWayIfPresent=true>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" class="${properties.kcHtmlClass!}">

<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="robots" content="noindex, nofollow">

    <#if properties.meta?has_content>
        <#list properties.meta?split(' ') as meta>
            <meta name="${meta?split('==')[0]}" content="${meta?split('==')[1]}"/>
        </#list>
    </#if>
    <title>${msg("loginTitle",(realm.displayName!''))}</title>
    <link rel="icon" href="${url.resourcesPath}/img/favicon.ico" />
    <#if properties.stylesCommon?has_content>
        <#list properties.stylesCommon?split(' ') as style>
            <link href="${url.resourcesCommonPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>
    <#if properties.styles?has_content>
        <#list properties.styles?split(' ') as style>
            <link href="${url.resourcesPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>
    <#if properties.scripts?has_content>
        <#list properties.scripts?split(' ') as script>
            <script src="${url.resourcesPath}/${script}" type="text/javascript"></script>
        </#list>
    </#if>
    <#if scripts??>
        <#list scripts as script>
            <script src="${script}" type="text/javascript"></script>
        </#list>
    </#if>
    <style>
        /* Include our custom styles inline for better compatibility */
        body {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            margin: 0;
            padding: 20px;
        }

        .login-pf {
            background: none;
        }

        .card-pf {
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(10px);
            border-radius: 20px;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
            border: 1px solid rgba(255, 255, 255, 0.2);
            max-width: 400px;
            margin: 50px auto;
            padding: 40px;
        }

        .kc-page-title, h1#kc-page-title {
            color: #333;
            font-size: 28px;
            font-weight: 600;
            text-align: center;
            margin-bottom: 30px;
            text-shadow: none;
        }

        .kc-page-title:before {
            content: "��";
            display: block;
            font-size: 48px;
            margin-bottom: 20px;
            text-align: center;
        }

        .form-group {
            margin-bottom: 25px;
        }

        .form-control, input[type="text"], input[type="password"], input[type="email"] {
            background: rgba(255, 255, 255, 0.8);
            border: 2px solid rgba(102, 126, 234, 0.3);
            border-radius: 12px;
            padding: 15px 20px;
            font-size: 16px;
            transition: all 0.3s ease;
            width: 100%;
            box-sizing: border-box;
        }

        .form-control:focus, input[type="text"]:focus, input[type="password"]:focus, input[type="email"]:focus {
            background: rgba(255, 255, 255, 1);
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
            outline: none;
        }

        .btn-primary, input[type="submit"] {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border: none;
            border-radius: 12px;
            padding: 15px 30px;
            font-size: 16px;
            font-weight: 600;
            width: 100%;
            transition: all 0.3s ease;
            text-transform: uppercase;
            letter-spacing: 1px;
            color: white;
            cursor: pointer;
        }

        .btn-primary:hover, input[type="submit"]:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 25px rgba(102, 126, 234, 0.3);
            background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
        }

        .control-label, label {
            color: #555;
            font-weight: 500;
            margin-bottom: 8px;
            display: block;
        }

        .alert-error, .kc-feedback-text {
            background: rgba(220, 53, 69, 0.1);
            border: 1px solid rgba(220, 53, 69, 0.3);
            color: #dc3545;
            border-radius: 10px;
            padding: 12px 16px;
            margin-bottom: 20px;
        }

        a {
            color: #667eea;
            text-decoration: none;
            font-weight: 500;
        }

        a:hover {
            color: #5a6fd8;
            text-decoration: underline;
        }

        #kc-info {
            background: rgba(102, 126, 234, 0.1);
            border-radius: 12px;
            padding: 20px;
            margin-top: 20px;
            text-align: center;
        }

        .card-pf {
            animation: fadeInUp 0.6s ease-out;
        }

        @keyframes fadeInUp {
            from {
                opacity: 0;
                transform: translateY(30px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
    </style>
</head>

<body class="${properties.kcBodyClass!}">
    <div class="${properties.kcLoginClass!}">
        <div id="kc-container" class="${properties.kcContainerClass!}">
            <div id="kc-container-wrapper" class="${properties.kcContainerWrapperClass!}">

                <div id="kc-header" class="${properties.kcHeaderClass!}">
                    <div id="kc-header-wrapper"
                         class="${properties.kcHeaderWrapperClass!}"><#nested "header"></div>
                </div>

                <div class="${properties.kcFormCardClass!}">
                    <header class="${properties.kcFormHeaderClass!}">
                        <#if realm.internationalizationEnabled  && locale.supported?size gt 1>
                            <div class="${properties.kcLocaleMainClass!}" id="kc-locale">
                                <div id="kc-locale-wrapper" class="${properties.kcLocaleWrapperClass!}">
                                    <div id="kc-locale-dropdown" class="${properties.kcLocaleDropDownClass!}">
                                        <a href="#" id="kc-current-locale-link">${locale.current}</a>
                                        <ul class="${properties.kcLocaleListClass!}">
                                            <#list locale.supported as l>
                                                <li class="${properties.kcLocaleListItemClass!}">
                                                    <a class="${properties.kcLocaleItemClass!}" href="${l.url}">${l.label}</a>
                                                </li>
                                            </#list>
                                        </ul>
                                    </div>
                                </div>
                            </div>
                        </#if>
                        <#if !(auth?has_content && auth.showUsername() && !auth.showResetCredentials())>
                            <#if displayRequiredFields>
                                <div class="${properties.kcContentWrapperClass!}">
                                    <div class="${properties.kcLabelWrapperClass!} subtitle">
                                        <span class="subtitle"><span class="required">*</span> ${msg("requiredFields")}</span>
                                    </div>
                                    <div class="col-md-10">
                                        <h1 id="kc-page-title"><#nested "header"></h1>
                                    </div>
                                </div>
                            <#else>
                                <h1 id="kc-page-title" class="kc-page-title"><#nested "header"></h1>
                            </#if>
                        <#else>
                            <#if displayRequiredFields>
                                <div class="${properties.kcContentWrapperClass!}">
                                    <div class="${properties.kcLabelWrapperClass!} subtitle">
                                        <span class="subtitle"><span class="required">*</span> ${msg("requiredFields")}</span>
                                    </div>
                                    <div class="col-md-10">
                                        <#nested "show-username">
                                        <div id="kc-username" class="${properties.kcFormGroupClass!}">
                                            <label id="kc-attempted-username">${auth.attemptedUsername}</label>
                                            <a id="reset-login" href="${url.loginRestartFlowUrl}">
                                                <div class="kc-login-tooltip">
                                                    <i class="${properties.kcResetFlowIcon!}"></i>
                                                    <span class="kc-tooltip-text">${msg("restartLoginTooltip")}</span>
                                                </div>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            <#else>
                                <#nested "show-username">
                                <div id="kc-username" class="${properties.kcFormGroupClass!}">
                                    <label id="kc-attempted-username">${auth.attemptedUsername}</label>
                                    <a id="reset-login" href="${url.loginRestartFlowUrl}">
                                        <div class="kc-login-tooltip">
                                            <i class="${properties.kcResetFlowIcon!}"></i>
                                            <span class="kc-tooltip-text">${msg("restartLoginTooltip")}</span>
                                        </div>
                                    </a>
                                </div>
                            </#if>
                        </#if>
                      </header>
                      <div id="kc-content">
                        <div id="kc-content-wrapper">

                          <#-- App-initiated actions should not see warning messages about the need to complete the action -->
                          <#-- during login.                                                                               -->
                          <#if displayMessage && message?has_content && (message.type != 'warning' || !isAppInitiatedAction??)>
                              <div class="alert-${message.type} ${properties.kcAlertClass!} pf-m-<#if message.type = 'error'>danger<#else>${message.type}</#if>">
                                  <div class="pf-c-alert__icon">
                                      <#if message.type = 'success'><span class="${properties.kcFeedbackSuccessIcon!}"></span></#if>
                                      <#if message.type = 'warning'><span class="${properties.kcFeedbackWarningIcon!}"></span></#if>
                                      <#if message.type = 'error'><span class="${properties.kcFeedbackErrorIcon!}"></span></#if>
                                      <#if message.type = 'info'><span class="${properties.kcFeedbackInfoIcon!}"></span></#if>
                                  </div>
                                      <span class="${properties.kcAlertTitleClass!}">${kcSanitize(message.summary)?no_esc}</span>
                              </div>
                          </#if>

                          <#nested "form">

                          <#if auth?has_content && auth.showTryAnotherWayLink() && showAnotherWayIfPresent>
                              <form id="kc-select-try-another-way-form" action="${url.loginAction}" method="post">
                                  <div class="${properties.kcFormGroupClass!}">
                                      <input type="hidden" name="tryAnotherWay" value="on"/>
                                      <a href="#" id="try-another-way"
                                         onclick="document.forms['kc-select-try-another-way-form'].submit();return false;">${msg("doTryAnotherWay")}</a>
                                  </div>
                              </form>
                          </#if>

                          <#if displayInfo>
                              <div id="kc-info" class="${properties.kcSignUpClass!}">
                                  <div id="kc-info-wrapper" class="${properties.kcInfoAreaWrapperClass!}">
                                      <#nested "info">
                                  </div>
                              </div>
                          </#if>
                        </div>
                      </div>

                </div>
            </div>
        </div>
    </div>
    <div class="kc-login-footer">
        Sistema de Controle Acadêmico
    </div>
</body>
</html>
</#macro>
