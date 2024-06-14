export function getObject (chunk, fromIndex) {
  const closingBraceIndex = chunk.indexOf('}', fromIndex)
  const event = chunk.slice(chunk.indexOf('{'), closingBraceIndex + 1)
  if (event) {
    try {
      JSON.parse(event)
      return event
    } catch (err) {
      return getObject(chunk, closingBraceIndex + 1)
    }
  }
}
