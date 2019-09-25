package kr.co.itcen.mysite.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.co.itcen.mysite.exception.UserDaoException;
import kr.co.itcen.mysite.vo.UserVo;

@Repository
public class UserDao {
	
	@Autowired
	private SqlSession sqlSession;
	
	@Autowired
	private DataSource dataSource;
	
	//UserService에서
	public UserVo get(UserVo vo) {
		UserVo result = sqlSession.selectOne("user.getByEmailAndPassword", vo);
		return result;
	}
	
	//로그인 하기위해 필요한 정보를 가져오는 get
	public UserVo get(String email, String password) {
		Map<String,String> map = new HashMap<String,String>();
		map.put("email", email);
		map.put("password",password);
		
		UserVo result = sqlSession.selectOne("user.getByEmailAndPassword", map);
		return result;		
	}
	
	//UserService에서
	public void update(UserVo vo) {
		if(vo.getPassword().equals("")) {
		update(vo.getNo(),vo.getName(),vo.getGender());
		}else {
		p_update(vo.getNo(),vo.getName(),vo.getPassword(),vo.getGender());
		}
		
	}
	
	
	//join을 하기위한 insert
	public Boolean insert(UserVo vo) throws UserDaoException { 
		int count =sqlSession.insert("user.insert",vo);
		return count == 1;		
	}
	


	//회원정보 수정을 위한 get
	public UserVo get(Long no) {
		UserVo result = null;

		Connection connection = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			connection = dataSource.getConnection();

			String sql = "select name,email,gender from user where no = ?  ";
			pstmt = connection.prepareStatement(sql);
			pstmt.setLong(1, no);

			rs = pstmt.executeQuery();
			if(rs.next()) {
				String name = rs.getString(1);
				String email = rs.getString(2);
				String gender = rs.getString(3);


				result = new UserVo();
				result.setName(name);
				result.setEmail(email);
				result.setGender(gender);
			}

		} catch (SQLException e) {
			System.out.println("error:" + e);
		} finally {
			try {
				if(rs != null) {
					rs.close();
				}
				if(pstmt != null) {
					pstmt.close();
				}

				if(pstmt != null) {
					pstmt.close();
				}

				if(connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return result;		
	}


	//password가 빈칸일 때 원래의 값을 update
	public boolean update(Long no,String name,String gender) {
		boolean result = false;

		Connection connection = null;
		PreparedStatement pstmt = null;
		

		try {
			connection = dataSource.getConnection();

			String sql = "update user set name =?, gender=? where no = ?";
			pstmt = connection.prepareStatement(sql);

			pstmt.setString(1, name);

			pstmt.setString(2, gender);

			pstmt.setLong(3, no);
			int count=pstmt.executeUpdate();
			
			result=(count==1);

		} catch (SQLException e) {
			System.out.println("error:" + e);
		} finally {
			try {
				if(pstmt != null) {
					pstmt.close();
				}

				if(pstmt != null) {
					pstmt.close();
				}

				if(connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return result;		
	}
	//password를 수정하였을 때 password까지 update
	public boolean p_update(Long no,String name,String password,String gender) {
		boolean result = false;

		Connection connection = null;
		PreparedStatement pstmt = null;
		

		try {
			connection = dataSource.getConnection();

			String sql = "update user set name =? , password=?, gender=? where no = ?";
			pstmt = connection.prepareStatement(sql);

			pstmt.setString(1, name);
			pstmt.setString(2, password);
			pstmt.setString(3, gender);

			pstmt.setLong(4, no);
			int count=pstmt.executeUpdate();
			
			result=(count==1);

		} catch (SQLException e) {
			System.out.println("error:" + e);
		} finally {
			try {
				if(pstmt != null) {
					pstmt.close();
				}

				if(pstmt != null) {
					pstmt.close();
				}

				if(connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return result;		
	}

}



