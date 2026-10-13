module.exports = {
  apps: [
    {
      name: 'anythingllm-server',
      cwd: '/opt/anything-llm/server',
      script: '/root/.nvm/versions/node/v18.20.8/bin/yarn',
      args: 'start',
      instances: 1,
      autorestart: true,
      max_memory_restart: '2G',
      env: {
        NODE_ENV: 'production',
        HOST: '0.0.0.0',
        PORT: 3001
      },
      log_date_format: 'YYYY-MM-DD HH:mm:ss',
      error_file: '/var/log/pm2/server-error.log',
      out_file: '/var/log/pm2/server-out.log'
    },
    {
      name: 'anythingllm-frontend',
      cwd: '/opt/anything-llm/frontend',
      script: '/root/.nvm/versions/node/v18.20.8/bin/yarn',
      args: 'start',
      instances: 1,
      autorestart: true,
      env: {
        NODE_ENV: 'production'
      },
      log_date_format: 'YYYY-MM-DD HH:mm:ss',
      error_file: '/var/log/pm2/frontend-error.log',
      out_file: '/var/log/pm2/frontend-out.log'
    },
    {
      name: 'anythingllm-collector',
      cwd: '/opt/anything-llm/collector',
      script: '/root/.nvm/versions/node/v18.20.8/bin/yarn',
      args: 'dev',
      instances: 1,
      autorestart: true,
      max_memory_restart: '1G',
      env: {
        NODE_ENV: 'production',
      },
      log_date_format: 'YYYY-MM-DD HH:mm:ss',
      error_file: '/var/log/pm2/collector-error.log',
      out_file: '/var/log/pm2/collector-out.log'
    }
  ]
};
