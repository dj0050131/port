import { useEffect, useState } from "react";
import System from "@/models/system";


export default function useSimpleSSO() {
  const [loading, setLoading] = useState(true);
  const [ssoConfig, setSsoConfig] = useState({
    enabled: false,
    noLogin: false,
    noLoginRedirect: null,
  });

  useEffect(() => {
    async function checkSsoConfig() {
      try {
        const settings = await System.keys();
        setSsoConfig({
          enabled: settings?.SimpleSSOEnabled,
          noLogin: settings?.SimpleSSONoLogin,
          noLoginRedirect: settings?.SimpleSSONoLoginRedirect,
        });
      } catch (e) {
        console.error(e);
      } finally {
        setLoading(false);
      }
    }
    checkSsoConfig();
  }, []);

  return { loading, ssoConfig };
}
