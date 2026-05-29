const https = require('https');
const querystring = require('querystring');
const crypto = require('crypto');
const {DateTime} = require('luxon');

const config = {
    endpoint: 'https://api.gname.com', // API address
    appid: 'your_appid', // APPID（See settings：https://gname.com/user#/dealer_api）
    appkey: 'your_appkey', // APPKEY（See settings：https://gname.com/user#/dealer_api）
    timezone: 'Asia/Shanghai', // Time zone，UTC+8
};

/**
 * Request the interface using POST method
 * @param {string} uri
 * @param {object} params Request parameters
 * @param {number} timeout Timeout setting (unit: seconds)
 * @return {Promise<object>}
 */
function postToApi(uri, params = {}, timeout = 10) {
    const timestamp = getTimestamp(config.timezone);
    if (timestamp === false) {
        return Promise.resolve({code: -1, msg: 'Error in obtaining timestamp'});
    }
    const url = `${config.endpoint}/${uri.replace(/^\//, '')}`;
    params.appid = config.appid;
    params.gntime = timestamp;
    params.gntoken = generateSign(params, config.appkey);

    const postData = querystring.stringify(params);

    const requestOptions = {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        timeout: timeout * 1000,
        rejectUnauthorized: false, // Disable SSL certificate verification
    };

    return new Promise((resolve, reject) => {
        const req = https.request(url, requestOptions, (res) => {
            let data = '';

            res.on('data', (chunk) => {
                data += chunk;
            });

            res.on('end', () => {
                const responseData = JSON.parse(data);
                resolve(responseData);
            });
        });

        req.on('error', (error) => {
            reject(error);
        });

        req.write(postData);
        req.end();
    });
}

/**
 * Generate Signature
 * @param {object} params Parameters to be signed
 * @param {string} appkey APPKEY
 * @return {string}
 */
function generateSign(params, appkey) {
    // First, sort the objects according to the ASCII code of their parameter names in ascending order.
    const sortedParams = Object.keys(params).sort().reduce((acc, key) => {
        acc[key] = params[key];
        return acc;
    }, {});
    // Concatenate the strings into the format key=value&key=value, and then URL-encode the value.
    let arr = []
    for (let key in sortedParams) {
        arr.push(`${key}=` + encodeURIComponent(sortedParams[key]));
    }
    const str = arr.join('&')
    // MD5 signature and convert to uppercase letters
    return crypto.createHash('md5').update(str + appkey).digest('hex').toUpperCase();
}

/**
 * Get the UTC timestamp of the specified time zone
 * @param {string} timezone It needs to be in the UTC-8 time zone, such as: Asia/Shanghai, Asia/Taipei
 * @return {number|false}
 */
function getTimestamp(timezone) {
    try {
        const currentTime = DateTime.local().setZone(timezone);
        return Math.floor(currentTime.toUTC().toMillis() / 1000);
    } catch (error) {
        return false;
    }
}


//Examples of Add domain template
postToApi('/api/template/add', {
    "name": "test",
    "xing": "Smith",
    "ming": "James",
    "email": "James@gname.com",
    "guojia": "SG",
    "province": "Singapore",
    "city": "Central Region",
    "address": "Tanjong Pagar",
    "gjqh": "65",
    "phone": "82563693",
    "youbian": "123456",
    "lang": "us"
})
    .then((result) => {
        if (result.code === 1) {
            console.log('Request successful', `Template ID:${result.data}`);
        } else {
            console.log(`Request failed,Error code:${result.code}，Error message：${result.msg}`);
        }
    })
    .catch((error) => {
        console.log('Request error:', error.message);
    });
