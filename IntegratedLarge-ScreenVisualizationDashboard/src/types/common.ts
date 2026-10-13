export interface ResponseResult<T> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

export interface PageResult<T> {
  total: number;
  list: T[];
}

export interface PaginationQuery {
  page?: number;
  size?: number;
  [key: string]: any;
}

