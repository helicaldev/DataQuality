var system = require('system');

// Web Address (URL) of the page to capture
var url = system.args[1];

url = url.replace(/#/gi, "&");

// File name of the captured image
var file = system.args[2];

var page = require('webpage').create();

// Browser size - height and width in pixels
// Change the viewport to 480x320 to emulate the iPhone
page.viewportSize = {
    width: 1245,
    height: 1755
};

page.paperSize = {
    format: 'A4',
    orientation: 'portrait',
    border: '1cm',
    footer: {
        height: "1.5cm",
        contents: phantom
            .callback(function (pageNum, numPages) {
                return "<div style='font-size:13px;font-weight:normal;'><span style='text-align:left'>Confidential</span><span style='margin-left:39em'>"
                    + pageNum + " of " + numPages + "</span></div>";
            })

    }
};

page.settings.localToRemoteUrlAccessEnabled = true;
page.settings.webSecurityEnabled = false;

// Set the User Agent String
// You can change it to iPad or Android for mobile screenshots
page.settings.userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/536.5 (KHTML, like Gecko) Chrome/19.0.1084.56 Safari/536.5";

// Render the screenshot image
page.open(url, function (status) {
    if (status !== "success") {
        console.log("Could not open web page : " + url);
        phantom.exit();
    } else {
        window.setTimeout(function () {
            page.render(file);
            console.log("Download the screenshot : " + file);
            phantom.exit();
        }, 1000);
    }
});