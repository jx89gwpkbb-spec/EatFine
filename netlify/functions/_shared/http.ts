export class HttpError extends Error {
  constructor(public status: number, message: string) {
    super(message);
  }
}

export const json = (data: unknown, status = 200) => Response.json(data, { status });

export async function readBody<T = Record<string, unknown>>(req: Request): Promise<T> {
  try {
    return (await req.json()) as T;
  } catch {
    throw new HttpError(400, "Invalid JSON body");
  }
}

export function handle(fn: (req: Request, params: Record<string, string>) => Promise<Response>) {
  return async (req: Request, context: { params: Record<string, string> }) => {
    try {
      return await fn(req, context.params ?? {});
    } catch (err) {
      if (err instanceof HttpError) return json({ error: err.message }, err.status);
      console.error(err);
      return json({ error: "Something went wrong" }, 500);
    }
  };
}

export const round2 = (n: number) => Math.round(n * 100) / 100;
