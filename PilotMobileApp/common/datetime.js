/** 原生日期时间选择（App-Plus） */
export function pickDateTime(initialTs) {
  const init = typeof initialTs === 'number' && initialTs > 0 ? new Date(initialTs) : new Date()
  return new Promise((resolve, reject) => {
    // #ifdef APP-PLUS
    try {
      plus.nativeUI.pickDate(
        (e) => {
          const d = e && e.date ? e.date : null
          if (!d) {
            reject(new Error('未选择日期'))
            return
          }
          plus.nativeUI.pickTime(
            (t) => {
              const td = t && t.date ? t.date : null
              if (!td) {
                reject(new Error('未选择时间'))
                return
              }
              const dt = new Date(d.getFullYear(), d.getMonth(), d.getDate(), td.getHours(), td.getMinutes(), 0)
              resolve(dt.getTime())
            },
            (err) => reject(err || new Error('取消选择时间')),
            { is24Hour: true }
          )
        },
        (err) => reject(err || new Error('取消选择日期')),
        { date: init }
      )
    } catch (e) {
      reject(e)
    }
    // #endif

    // #ifndef APP-PLUS
    uni.showToast({ title: '请在真机/App运行以选择时间', icon: 'none' })
    reject(new Error('当前平台不支持原生时间选择'))
    // #endif
  })
}

