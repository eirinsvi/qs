# qs_frontend

## Configure ip-address
 - go to qs_frontend\src\services\axiosService.js
 - change the current ip address in baseURL in apiClient to your ipv4 address in wifi
```
baseURL: 'http://*your ipv4 address in wifi*:8080'  
```

## Project setup
```
npm install
```

### Compiles and hot-reloads for development
```
npm run serve
```
In order to use this application, use network URL
![](../feature-student/Skjermbilde%202022-04-04%20kl.%2015.29.07.png)

### Compiles and minifies for production
```
npm run build
```

### Run your unit tests
```
npm run test:unit
```

### Lints and fixes files
```
npm run lint
```

### Customize configuration
See [Configuration Reference](https://cli.vuejs.org/config/).
