import sys

import requests
import hashlib
from urllib.parse import quote
from datetime import datetime, timezone, timedelta

config = {
    'endpoint': 'https://api.gname.com',  # API address
    'appid': 'your_appid',  # APPID（See settings：https://gname.com/user#/dealer_api）
    'appkey': 'your_appkey',  # APPKEY（See settings：https://gname.com/user#/dealer_api）
    'timezone': 8,  #Time zone，UTC+8
}


def post_to_api(uri, params=None, timeout=10):
    """
    Request the interface using POST method
    :param string uri:
    :param params:Request parameters
    :param int timeout: Timeout setting (unit: seconds)
    :return:
    """
    config_copy = config.copy()
    timestamp = get_timestamp(config_copy['timezone'])
    if timestamp is None:
        return {'code': -1, 'msg': 'Error in obtaining timestamp'}

    url = config_copy['endpoint'] + '/' + uri.strip('/')
    params = params or {}
    params['appid'] = config_copy['appid']
    params['gntime'] = timestamp
    params['gntoken'] = generate_sign(params, config_copy['appkey'])

    try:
        response = requests.post(url, data=params, timeout=timeout)
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        return {'code': -1, 'msg': 'HTTP Error: ' + str(e)}


def generate_sign(params, appkey):
    """
    Generate Signature
    :param params:
    :param appkey:
    :return:
    """

    # First, sort the dictionary by key name.
    sorted_params = sorted(params.items(), key=lambda x: x[0])
    # URL-encode the parameter value
    sorted_params = [(k, quote(str(v))) for k, v in sorted_params]
    # Concatenate parameter key-value pairs
    query_string = '&'.join([f'{k}={v}' for k, v in sorted_params])

    # Construct the reception signature string and add the APPKEY.
    str_to_sign = query_string + appkey
    # Calculate the MD5 hash and convert it to uppercase.
    signature = hashlib.md5(str_to_sign.encode('utf-8')).hexdigest().upper()
    return signature


def get_timestamp(timezone_hours):
    """
    Get the UTC timestamp of the specified time zone
    :param timezone_hours:
    :return:
    """
    try:
        tz = timezone(timedelta(hours=int(timezone_hours)))
        dt = datetime.now(tz)
        return int(dt.timestamp())
    except Exception:
        return None


if __name__ == '__main__':
    # Examples of Add domain template
    result = post_to_api('/api/template/add', {
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
    if result['code'] == 1:
        print("Request successful")
        print(f"Template ID:{result['data']}")
    else:
        print(f"Request failed,Error code:{result['code']},Error message:{result['msg']}")
