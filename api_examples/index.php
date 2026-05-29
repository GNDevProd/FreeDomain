<?php
$config = [
    'endpoint' => 'https://api.gname.com',//API address
    'appid' => 'your_appid',//APPID（See settings：https://gname.com/user#/dealer_api）
    'appkey' => 'your_appkey',//APPKEY（See settings：https://gname.com/user#/dealer_api）
    'timezone' => 'Asia/Shanghai',//Time zone，UTC+8
];

/**
 * Request the interface using POST method
 * @param string $uri
 * @param array $params Request parameters
 * @param integer $timeout Timeout setting (unit: seconds)
 * @return array|mixed
 */
function post_to_api($uri, $params = [], $timeout = 10)
{
    global $config;
    $timestamp = get_timestamp($config['timezone']);
    if ($timestamp === false) {
        return ['code' => -1, 'msg' => 'Error in obtaining timestamp'];
    }
    $url = $config['endpoint'] . '/' . ltrim($uri, '/');
    $params['appid'] = $config['appid'];
    $params['gntime'] = $timestamp;
    $params['gntoken'] = generate_sign($params, $config['appkey']);

    $curl = curl_init();
    curl_setopt($curl, CURLOPT_URL, $url);
    curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, false);
    curl_setopt($curl, CURLOPT_HTTPHEADER, [
        'Content-Type: application/x-www-form-urlencoded'
    ]);
    curl_setopt($curl, CURLOPT_TIMEOUT, $timeout);
    curl_setopt($curl, CURLOPT_CONNECTTIMEOUT, $timeout);
    curl_setopt($curl, CURLOPT_POST, true);
    if (!empty($params)) {
        curl_setopt($curl, CURLOPT_POSTFIELDS, http_build_query($params));
    }
    $json = curl_exec($curl);
    if ($json === false) {
        return ['code' => -1, 'msg' => 'cURL Error: ' . curl_error($curl)];
    }
    $httpCode = curl_getinfo($curl, CURLINFO_HTTP_CODE);
    if ($httpCode !== 200) {
        return ['code' => -1, 'msg' => 'HTTP status code is ' . $httpCode];
    }
    curl_close($curl);
    return json_decode($json, true);
}

/**
 * Generate Signature
 * @param array $params Parameters to be signed
 * @param string $appkey APPKEY
 * @return string
 */
function generate_sign($params, $appkey)
{
    //First sort the array according to the ASCII code of the parameter name from small to large
    ksort($params);
    //According to the key=value format, the value needs to be URLEncode encoded
    $arr = [];
    foreach ($params as $k => $v) {
        $arr[] = "{$k}=" . urlencode($v);
    }
    $str = implode('&', $arr);
    //MD5 signature and converted to uppercase letters
    return strtoupper(md5($str . $appkey));
}

/**
 * Get the UTC timestamp of the specified time zone
 * @param string $timezoneName It needs to be in the UTC-8 time zone, such as: Asia/Shanghai, Asia/Taipei
 * @return int|false
 */
function get_timestamp($timezoneStr)
{
    try {
        $timezone = new DateTimeZone($timezoneStr);
        $dateTime = new DateTime('now', $timezone);
        return $dateTime->getTimestamp();
    } catch (\Exception $exception) {
        return false;
    }
}

//Examples of Add domain template
$result = post_to_api('/api/template/add', [
    "name" => "test",
    "xing" => "Smith",
    "ming" => "James",
    "email" => "James@gname.com",
    "guojia" => "SG",
    "province" => "Singapore",
    "city" => "Central Region",
    "address" => "Tanjong Pagar",
    "gjqh" => "65",
    "phone" => "82563693",
    "youbian" => "123456",
    "lang" => "us"
]);
if ($result['code'] === 1) {
    echo "Request successful" . PHP_EOL;
    echo "Template ID:{$result['data']}" . PHP_EOL;
} else {
    echo "Request failed,Error code:{$result['code']}，Error message：{$result['msg']}" . PHP_EOL;
}
