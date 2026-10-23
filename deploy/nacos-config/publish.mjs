#!/usr/bin/env node
/**
 * 把 deploy/nacos-config 下的 YAML 发布到 Nacos（Config as Code）。
 *
 * 用法：
 *   node deploy/nacos-config/publish.mjs                    # 发布 base + services 到 NACOS_NS
 *   node deploy/nacos-config/publish.mjs --with-local       # 额外发布 local/ 覆盖（本地联调）
 *   NACOS_NS=test node deploy/nacos-config/publish.mjs      # 发布到测试环境命名空间
 *
 * 环境变量：NACOS_ADDR / NACOS_NS / NACOS_USERNAME / NACOS_PASSWORD
 */
import { readFile, readdir } from 'node:fs/promises';
import { join, basename } from 'node:path';
import { fileURLToPath } from 'node:url';

const configRoot = fileURLToPath(new URL('.', import.meta.url));
const addr = (process.env.NACOS_ADDR || 'http://127.0.0.1:8848').replace(/\/+$/, '');
const namespace = process.env.NACOS_NS || '';
const username = process.env.NACOS_USERNAME || 'nacos';
const password = process.env.NACOS_PASSWORD || 'nacos';
const withLocal = process.argv.includes('--with-local');

const dirs = ['base', 'services', ...(withLocal ? ['local'] : [])];

async function login() {
  const body = new URLSearchParams({ username, password });
  const res = await fetch(`${addr}/nacos/v1/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body
  });
  if (!res.ok) {
    throw new Error(`Nacos 登录失败: HTTP ${res.status} ${await res.text()}`);
  }
  return (await res.json()).accessToken;
}

async function publish(token, dataId, content) {
  const body = new URLSearchParams({
    dataId,
    group: 'DEFAULT_GROUP',
    tenant: namespace,
    type: 'yaml',
    content
  });
  if (token) body.set('accessToken', token);
  const res = await fetch(`${addr}/nacos/v1/cs/configs`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body
  });
  if (!res.ok) {
    throw new Error(`发布 ${dataId} 失败: HTTP ${res.status} ${await res.text()}`);
  }
  console.log(`published ${dataId} -> namespace=${namespace || 'public'}`);
}

const token = await login();
for (const dir of dirs) {
  const abs = join(configRoot, dir);
  const files = (await readdir(abs)).filter((name) => name.endsWith('.yaml'));
  for (const file of files) {
    const content = await readFile(join(abs, file), 'utf8');
    // dataId 直接用文件名：application.yaml / redis.yaml / mysql.yaml / order-service.yaml
    await publish(token, basename(file), content);
  }
}
console.log(`done: ${dirs.join(', ')}`);
